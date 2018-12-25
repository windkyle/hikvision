package cn.ipanel.apps.dj.hikvision.service;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author luzh
 * createTime 2017年7月3日 下午2:44:46
 */
@Component
public class HttpService {

	private static Logger logger = LoggerFactory.getLogger(HttpService.class);

	@Autowired
	private CloseableHttpClient httpClient;

	/**
	 * 不带参数的get请求，如果状态码为200，则返回body，如果不为200，则返回null
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public String doGet(String url, String charset) throws Exception {
		CloseableHttpResponse response = null;
		String result = null;
		try {
			// 声明 http get 请求
			HttpGet httpGet = new HttpGet(url);

			// 发起请求
			response = this.httpClient.execute(httpGet);

			// 判断状态码是否为200
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// 返回响应体的内容
				result = EntityUtils.toString(response.getEntity(), charset);
			} else {
			    EntityUtils.consume(response.getEntity());
			}
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
		    if (response != null) {
                EntityUtils.consume(response.getEntity());
            }
		}
	}
	
	public String doJsonPost(String url, String json, String charset) throws Exception {
        CloseableHttpResponse response = null;
        String result = null;
        try {
            // 声明httpPost请求
            HttpPost httpPost = new HttpPost(url);

            // 给httpPost设置JSON格式的参数
            StringEntity requestEntity = new StringEntity(json, "utf-8");
            requestEntity.setContentEncoding("utf-8");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setEntity(requestEntity);

            // 发起请求
            response = this.httpClient.execute(httpPost);
            // 判断状态码是否为200
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                // 返回响应体的内容
                result = EntityUtils.toString(response.getEntity(), charset);
            } else {
                EntityUtils.consume(response.getEntity());
                logger.info("url: {}, json: {}, statuscode: {}.", url, json, response.getStatusLine().getStatusCode());
                throw new Exception("http请求失败:" + response.getStatusLine().getStatusCode());
            }
            return result;
        } catch (Exception e) {
            logger.error("by luzh-> httpService: {}.", e.getMessage());
            throw e;
        } finally {
            if (response != null) {
                EntityUtils.consume(response.getEntity());
            }
        }
    }
}