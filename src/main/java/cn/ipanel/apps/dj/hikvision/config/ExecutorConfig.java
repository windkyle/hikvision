package cn.ipanel.apps.dj.hikvision.config;

import cn.ipanel.apps.dj.hikvision.service.AlarmBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

@Configuration
public class ExecutorConfig {

    private int corePoolSize = 10;
    private int maxPoolSize = 50;
    private int queueCapacity = 30000;
    private int awaitTerminationSeconds = 60;

    @Bean
    public Executor myAsync() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("AlarmMonitorExecutor-");
        // rejection-policy：当pool已经达到max size的时候，如何处理新任务
        // CALLER_RUNS：不在新线程中执行任务，而是有调用者所在的线程来执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);
        executor.initialize();
        Runtime.getRuntime().addShutdownHook(new Thread(executor::shutdown));
        return executor;
    }


    @Bean
    public Executor alarmPool() {
        return new ForkJoinPool(1, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true);
    }

    @Bean
    public BlockingQueue<AlarmBean> alarmQueue() {
        return new ArrayBlockingQueue<>(100000);
    }
}
