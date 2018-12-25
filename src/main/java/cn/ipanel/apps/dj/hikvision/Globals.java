package cn.ipanel.apps.dj.hikvision;

import cn.ipanel.apps.dj.hikvision.service.RedisAlarmBean;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Globals {

    public static String formatDateTime(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static String makeRedisKey(RedisAlarmBean alarmBean) {
        return alarmBean.getLongTime() + "-" + alarmBean.getDevice();
    }
}
