package cn.ipanel.apps.dj.hikvision;

import cn.ipanel.apps.dj.hikvision.config.HikvisionClientConfig;
import cn.ipanel.apps.dj.hikvision.config.SystemConfig;
import cn.ipanel.apps.dj.hikvision.service.MyService;
import cn.ipanel.apps.dj.hikvision.task.AlarmTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.List;

@Component
@Order(value = 1)
public class StartUpRunner implements CommandLineRunner {

    private static Logger logger = LoggerFactory.getLogger(StartUpRunner.class);

    @Resource
    private MyService myService;

    @Resource
    private AlarmTask alarmTask;

    @Resource
    private SystemConfig systemConfig;

    @Override
    public void run(String... args) throws Exception {
        Field[] fields = systemConfig.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Object object = field.get(systemConfig);
            if (StringUtils.isEmpty(object)) {
                throw new Exception("config field is empty: " + field.getName());
            }
        }

        alarmTask.listemAlarmQueue();
        try {
            myService.initDevice();
            logger.info("system start listen device success");
        } catch (Exception e) {
            logger.error("system start listen device failed: {}", e.getMessage(), e);
        }
        alarmTask.exportAlarmWithStartUp();
    }
}