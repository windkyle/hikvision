package cn.ipanel.apps.dj.hikvision;

import cn.ipanel.apps.dj.hikvision.service.AlarmBean;
import cn.ipanel.apps.dj.hikvision.service.RedisAlarmBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.SpringServletContainerInitializer;

import java.util.concurrent.ConcurrentHashMap;

@EnableScheduling
@EnableAsync
@SpringBootApplication
public class HikvisionApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(HikvisionApplication.class, args);
	}

	@Bean
	public ConcurrentHashMap<Long, RedisAlarmBean> alarmBeanMap() {
		return new ConcurrentHashMap<>(100);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(HikvisionApplication.class);
	}
}
