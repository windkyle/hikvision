package cn.ipanel.apps.dj.hikvision.config;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.HttpConnectionFactory;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultHttpResponseParserFactory;
import org.apache.http.impl.conn.ManagedHttpClientConnectionFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.impl.io.DefaultHttpRequestWriterFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * @author luzh
 * createTime 2017年11月10日 上午11:26:32
 */
@Configuration
public class HttpClient {
    
    private static CloseableHttpClient httpClient = null;
	
    private Integer maxTotal = 200;

    private Integer defaultMaxPerRoute = 100;

    private Integer connectTimeout = 2000;

    private Integer connectionRequestTimeout = 2000;

    private Integer socketTimeout = 5000;
    
    private int retryTime = 2;  
    
    /**
     * 请求重试处理
     * @return handler
     */
    @Bean
    public HttpRequestRetryHandler httpRequestRetryHandler() {  
        // 请求重试  
        final int retryTime = this.retryTime;  
        return (exception, executionCount, context) -> {
            // Do not retry if over max retry count,如果重试次数超过了retryTime,则不再重试请求
            if (executionCount >= retryTime) {
                return false;
            }
            // 服务端断掉客户端的连接异常
            if (exception instanceof NoHttpResponseException) {
                return true;
            }
            // time out 超时重试
            if (exception instanceof InterruptedIOException) {
                return true;
            }
            // Unknown host
            if (exception instanceof UnknownHostException) {
                return false;
            }
            // SSL handshake exception
            if (exception instanceof SSLException) {
                return false;
            }
            HttpClientContext clientContext = HttpClientContext.adapt(context);
            HttpRequest request = clientContext.getRequest();
            return !(request instanceof HttpEntityEnclosingRequest);
        };
    }  
      
    /**
     * 首先实例化一个连接池管理器，设置最大连接数、并发连接数
     * @return manager
     */
    @Bean(name = "httpClientConnectionManager")
    public PoolingHttpClientConnectionManager getHttpClientConnectionManager(){
        // 注册访问协议相关的Socket 工厂
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", SSLConnectionSocketFactory.getSystemSocketFactory())
                .build();
        
        // HttpConnection 工厂：配置写请求/解析响应处理器
        HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connectionFactory = new ManagedHttpClientConnectionFactory(
                DefaultHttpRequestWriterFactory.INSTANCE, 
                DefaultHttpResponseParserFactory.INSTANCE);
        
        // DNS 解析器
        DnsResolver dnsResolver = SystemDefaultDnsResolver.INSTANCE;
        
        PoolingHttpClientConnectionManager httpClientConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry, connectionFactory, dnsResolver);
        
        // 默认为Socket 配置
        SocketConfig defaultSocketConfig = SocketConfig.custom().setTcpNoDelay(true).build();
        httpClientConnectionManager.setDefaultSocketConfig(defaultSocketConfig);
        // 设置整个连接池的最大连接数
        httpClientConnectionManager.setMaxTotal(maxTotal);
        /*
         * 每个路由的默认最大连接，每个路由实际最大连接数默认为DefaultMaxPerRoute控制，而MaxTotal是控制整个池最大数
         * 设置过小无法支持大并发(ConnectionPoolTimeoutException: Timeout waiting for connection from pool), 路由是对maxTotal的细分
         */
        httpClientConnectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);
        // 在从连接池获取连接时，连接不活跃多长时间后需要进行一次验证，默认为2s
        httpClientConnectionManager.setValidateAfterInactivity(5 * 1000);
        return httpClientConnectionManager;
    }

    /**
     * 实例化连接池，设置连接池管理器。
     * 这里需要以参数形式注入上面实例化的连接池管理器
     * @param httpClientConnectionManager manager
     * @return builder
     */
    @Bean(name = "httpClientBuilder")
    public HttpClientBuilder getHttpClientBuilder(@Qualifier("httpClientConnectionManager")PoolingHttpClientConnectionManager httpClientConnectionManager){

        //HttpClientBuilder中的构造方法被protected修饰，所以这里不能直接使用new来实例化一个HttpClientBuilder，可以使用HttpClientBuilder提供的静态方法create()来获取HttpClientBuilder对象
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        httpClientBuilder.setConnectionManager(httpClientConnectionManager).setConnectionManagerShared(true);
        // 设置重试handler
        httpClientBuilder.setRetryHandler(httpRequestRetryHandler());

        return httpClientBuilder;
    }

    /**
     * 注入连接池，用于获取httpClient
     * @param httpClientBuilder builder
     * @return client
     */
    @Bean
    public CloseableHttpClient getCloseableHttpClient(@Qualifier("httpClientBuilder") HttpClientBuilder httpClientBuilder){
        if (httpClient == null) {
            // 默认请求配置
            RequestConfig defaultRequestConfig = RequestConfig.custom()
                    .setConnectTimeout(connectTimeout) // 设置连接超时时间，2s
                    .setSocketTimeout(socketTimeout) // 设置等待数据超时时间，5s
                    .setConnectionRequestTimeout(connectionRequestTimeout) // 设置从连接池获取连接的等待超时时间
                    .build();
            
            httpClient = httpClientBuilder
                    .evictIdleConnections(60, TimeUnit.SECONDS) // 定期回收空闲连接
                    .evictExpiredConnections() // 定期回收过期连接
                    .setConnectionTimeToLive(60, TimeUnit.SECONDS) // 连接存活时间，如果不设置，则根据长连接信息决定
                    .setDefaultRequestConfig(defaultRequestConfig) // 设置默认请求配置
                    .setConnectionReuseStrategy(DefaultConnectionReuseStrategy.INSTANCE) // 连接重用策略，即是否能keepAlive
                    .setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE) // 长连接配置，即获取长连接生成多长时间
                    .build();
            
            // JVM 停止或重启时，关闭连接池释放掉连接(与数据库连接池类似)
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    httpClient.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        }
        return httpClient;
    }
}