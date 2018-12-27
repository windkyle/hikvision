package cn.ipanel.apps.dj.hikvision.task;

import cn.ipanel.apps.dj.hikvision.Globals;
import cn.ipanel.apps.dj.hikvision.config.SystemConfig;
import cn.ipanel.apps.dj.hikvision.service.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class AlarmTask {

    private static Logger logger = LoggerFactory.getLogger(AlarmTask.class);

    private static final String dataFileName = "-data.json";

    private Gson gson = new Gson();

    private ConcurrentHashMap<Long, RedisAlarmBean> alarmBeanMap;
    private BlockingQueue<AlarmBean> alarmQueue;
    private MyService myService;
    private MyRedisService myRedisService;
    private HttpService httpService;
    private SystemConfig systemConfig;

    @Autowired
    public AlarmTask(BlockingQueue<AlarmBean> alarmQueue, MyService myService, MyRedisService myRedisService,
                     HttpService httpService, ConcurrentHashMap<Long, RedisAlarmBean> alarmBeanMap,
                     SystemConfig systemConfig) {
        this.alarmQueue = alarmQueue;
        this.myService = myService;
        this.myRedisService = myRedisService;
        this.httpService = httpService;
        this.alarmBeanMap = alarmBeanMap;
        this.systemConfig = systemConfig;
    }

    /**
     * 监听打卡数据队列，收到报警保存至redis中并导出到外部接口
     */
    @Async("alarmPool")
    public void listemAlarmQueue() {
        while (true) {
            AlarmBean bean = new AlarmBean();
            try {
                logger.info("waiting alarmQueueData");
                bean = alarmQueue.take();
                logger.info("get data from alarmQueueData");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                logger.info("some one sign in at:{}, card number:{}, device:{}",
                        bean.getTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        bean.getCard(), bean.getDevice());
                RedisAlarmBean redisAlarmBean = new RedisAlarmBean(bean);
                myRedisService.saveNotExportAlarm(redisAlarmBean);
                exportAlarm(redisAlarmBean);
            } catch (Exception e) {
                logger.info("listen alarmQueue error: {}", e.getMessage());
            }
        }
    }

    /**
     * 开始导出到外部接口，如果导出过程中失败，将数据添加到全局map中一遍定时重试
     * @param alarmBean
     */
    @Async("myAsync")
    public void exportAlarm(RedisAlarmBean alarmBean) {
        try {
            ExportResultBean resultBean = gson.fromJson(
                    httpService.doJsonPost(
                            systemConfig.getExportUrl(),
                            gson.toJson(new ExportBean(alarmBean.getCard(), alarmBean.getLongTime(), alarmBean.getDevice())),
                            "utf-8"
                    ),
                    ExportResultBean.class
            );
            if (!StringUtils.isEmpty(resultBean.getResult()) && resultBean.getResult().equals(ExportResultBean.successResult)) {
                alarmBean.setHasExprotToExternalSystem(true);
                alarmBean.setExportToExternalSystemTime(Instant.now().getEpochSecond());
                myRedisService.saveAlarm(alarmBean);
                myRedisService.removeNotExportAlarm(alarmBean);
                logger.info("export success-> card:{}, time:{}, exportTime:{}", alarmBean.getCard(), Globals.formatDateTime(alarmBean.getTime()), Globals.formatDateTime(LocalDateTime.now()));
            } else {
                logger.error("export failed-> card:{}, time:{}, result:{}", alarmBean.getCard(), Globals.formatDateTime(alarmBean.getTime()), gson.toJson(resultBean));
                alarmBeanMap.put(alarmBean.getLongTime(), alarmBean);
            }
        } catch (Exception e) {
            logger.error("export error-> card:{}, time:{}, reason:{}", alarmBean.getCard(), alarmBean.getTime(),e.getMessage());
            alarmBeanMap.put(alarmBean.getLongTime(), alarmBean);
        }
    }

    /**
     * 项目启动读取所有未成功导出数据重新导出到外部接口
     */
    @Async("myAsync")
    public void exportAlarmWithStartUp() {
        List<RedisAlarmBean> list = myRedisService.getNotExportAlarm();
        if (list.isEmpty()) {
            logger.info("no info need to export");
            return;
        }
        logger.info("find {} info, start export.", list.size());
        list.parallelStream().forEach(bean -> {
            exportAlarm(bean);
        });
    }

    /**
     * 每30分钟初始化设备重新布防监听
     */
    @Scheduled(cron = "0 */30 * * * *")//@Scheduled(cron = "0 */5 * * * *")
    public void initAgain() {
        logger.info("init again {}", Globals.formatDateTime(LocalDateTime.now()));
        initAginMethod();
    }

    /**
     * 每隔5分钟重试导出至外部接口
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void exportAgain() {
        logger.info("batch export again {}", Globals.formatDateTime(LocalDateTime.now()));
        alarmBeanMap.forEach((key, value) -> {
            alarmBeanMap.remove(key);
            exportAlarm(new RedisAlarmBean(value));
        });
    }

    /**
     * 初始化指纹机并重新连接布防监听
     */
    private void initAginMethod() {
        try {
            myService.initAgain();
        } catch (Exception e) {
            CountDownLatch latch = new CountDownLatch(1);
            try {
                latch.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException e1) {
                // nothing
            } finally {
                initAginMethod();
            }
        }
    }
    /**
     * 每2小时保存一次数据到文件中
     */
    @Scheduled(cron = "0 0 */2 * * *")
    public void saveData() {
        logger.info("save info to hd, {}", Globals.formatDateTime(LocalDateTime.now()));
        List<RedisAlarmBean> list = myRedisService.getAlarm();
        list.stream().collect(Collectors.groupingBy(info -> info.getTime().getYear())).forEach((key, value) -> {
            value.stream().collect(Collectors.groupingBy(info -> info.getTime().getMonthValue())).forEach((key1, value1) -> {
                value1.stream().collect(Collectors.groupingBy(info -> info.getTime().getDayOfMonth())).forEach( (key2, value2) -> {
                    value2.stream().collect(Collectors.groupingBy(info -> info.getDevice())).forEach((key3, value3)  ->{
                        logger.info("start save {}-{}-{} devie {} data to HD", key, key1, key2, key3);
                        String folderPath = systemConfig.getDataSavePath() + "/hikvision/" + key + "/" + key1 + "/" + key2;
                        File folder = new File(folderPath);
                        if (!folder.exists() && !folder.mkdirs()) {
                            logger.error("folder {} not exists or can't create folder!", folderPath);
                            return;
                        }
                        String filePath = folderPath + "/" + key3 + dataFileName;
                        File dataFile = new File(filePath);
                        List<HdAlarmInfoBean> hdList = null;
                        if (!dataFile.exists()) {
                            try {
                                if (!dataFile.createNewFile()) {
                                    throw new Exception("create data file failed");
                                }
                                hdList = new ArrayList<>(10);
                            } catch (Exception e) {
                                logger.error("create data file {} error: {}", filePath, e.getMessage());
                                return;
                            }
                        } else {
                            try {
                                String text = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8).collect(Collectors.joining());
                                hdList = gson.fromJson(text, new TypeToken<List<HdAlarmInfoBean>>(){}.getType());
                            } catch (IOException e) {
                                logger.error("read file {} error: {}", filePath, e.getMessage());
                                return;
                            }
                        }
                        hdList.addAll(value3.stream().map(HdAlarmInfoBean::new).collect(Collectors.toList()));
                        List<String> redisKeys = value3.stream().map(Globals::makeRedisKey).collect(Collectors.toList());
                        OutputStream outputStream = null;
                        try {
                            outputStream = new FileOutputStream(dataFile);
                            outputStream.write(gson.toJson(hdList).getBytes());
                            outputStream.close();
                            myRedisService.removeAlarm(redisKeys);
                            logger.info("save {}-{}-{} devie {} data to HD success", key, key1, key2, key3);
                        } catch (Exception e) {
                            if (null != outputStream){
                                try {
                                    outputStream.close();
                                } catch (IOException e1) {
                                }
                            }
                            logger.info("write to file {} error {}", filePath, e.getMessage());
                            return;
                        }

                    });
                });
            });
        });
    }
    /**
     * 每2小时保存一次数据到文件中
     */
    //@Scheduled(cron = "0 0 */2 * * *")
/*    public void saveData() {
        logger.info("save info to hd, {}", Globals.formatDateTime(LocalDateTime.now()));
        List<RedisAlarmBean> list = myRedisService.getAlarm();
        list.stream().collect(Collectors.groupingBy(info -> info.getTime().getYear())).forEach((key, value) -> {
            value.stream().collect(Collectors.groupingBy(info -> info.getTime().getMonthValue())).forEach((key1, value1) -> {
                value1.stream().collect(Collectors.groupingBy(info -> info.getTime().getDayOfMonth())).forEach( (key2, value2) -> {
                    logger.info("start save {}-{}-{} devie {} data to HD", key, key1, key2, systemConfig.getDevice());
                    String folderPath = systemConfig.getDataSavePath() + "/hikvision/" + key + "/" + key1 + "/" + key2;
                    File folder = new File(folderPath);
                    if (!folder.exists() && !folder.mkdirs()) {
                        logger.error("folder {} not exists or can't create folder!", folderPath);
                        return;
                    }
                    String filePath = folderPath + "/" + systemConfig.getDevice() + dataFileName;
                    File dataFile = new File(filePath);
                    List<HdAlarmInfoBean> hdList = null;
                    if (!dataFile.exists()) {
                        try {
                            if (!dataFile.createNewFile()) {
                                throw new Exception("create data file failed");
                            }
                            hdList = new ArrayList<>(10);
                        } catch (Exception e) {
                            logger.error("create data file {} error: {}", filePath, e.getMessage());
                            return;
                        }
                    } else {
                        try {
                            String text = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8).collect(Collectors.joining());
                            hdList = gson.fromJson(text, new TypeToken<List<HdAlarmInfoBean>>(){}.getType());
                        } catch (IOException e) {
                            logger.error("read file {} error: {}", filePath, e.getMessage());
                            return;
                        }
                    }
                    hdList.addAll(value2.stream().map(HdAlarmInfoBean::new).collect(Collectors.toList()));
                    List<String> redisKeys = value2.stream().map(Globals::makeRedisKey).collect(Collectors.toList());
                    OutputStream outputStream = null;
                    try {
                        outputStream = new FileOutputStream(dataFile);
                        outputStream.write(gson.toJson(hdList).getBytes());
                        outputStream.close();
                        myRedisService.removeAlarm(redisKeys);
                        logger.info("save {}-{}-{} devie {} data to HD success", key, key1, key2, systemConfig.getDevice());
                    } catch (Exception e) {
                        if (null != outputStream){
                            try {
                                outputStream.close();
                            } catch (IOException e1) {
                            }
                        }
                        logger.info("write to file {} error {}", filePath, e.getMessage());
                        return;
                    }
                });
            });
        });
    }   */
}
