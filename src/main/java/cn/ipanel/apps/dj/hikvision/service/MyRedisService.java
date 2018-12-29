package cn.ipanel.apps.dj.hikvision.service;

import cn.ipanel.apps.dj.hikvision.Globals;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MyRedisService {

    private static final String redisKey = "hikvisionAlarm";
    private static final String notExportRedisKey = "hikvisionAlarm-NotExport";

    private RedisTemplate<String, String> redisTemplate;

    private Gson gson = new Gson();

    @Autowired
    public MyRedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveAlarm(RedisAlarmBean bean) {
        redisTemplate.opsForHash().put(redisKey, Globals.makeRedisKey(bean), gson.toJson(bean));
    }

    public void saveNotExportAlarm(RedisAlarmBean bean) {
        redisTemplate.opsForHash().put(notExportRedisKey, Globals.makeRedisKey(bean), gson.toJson(bean));
    }

    public void removeNotExportAlarm(RedisAlarmBean bean) {
        redisTemplate.opsForHash().delete(notExportRedisKey, Globals.makeRedisKey(bean));
    }

    public List<RedisAlarmBean> getNotExportAlarm() {
        List<RedisAlarmBean> list = new ArrayList<>(10);
        redisTemplate.opsForHash().values(notExportRedisKey).forEach( object -> {
            RedisAlarmBean alarmBean = gson.fromJson(object.toString(), RedisAlarmBean.class);
/*            if (alarmBean.getDevice().equals(device)) {
                list.add(alarmBean);
            }*/
            if(!alarmBean.getHasExprotToExternalSystem()){
                list.add(alarmBean);
            }
        });
        return list;
    }

    public List<RedisAlarmBean> getAlarm() {
        List<RedisAlarmBean> list = new ArrayList<>(200);
        redisTemplate.opsForHash().values(redisKey).forEach(object -> {
            RedisAlarmBean alarmBean = gson.fromJson(object.toString(), RedisAlarmBean.class);
/*            if (alarmBean.getDevice().equals(device)) {
                list.add(alarmBean);
            }*/
            list.add(alarmBean);
        });
        return list;
    }

    public void removeAlarm(List<String> keys) {
        redisTemplate.opsForHash().delete(redisKey, keys.toArray(new String[] {}));
    }
}
