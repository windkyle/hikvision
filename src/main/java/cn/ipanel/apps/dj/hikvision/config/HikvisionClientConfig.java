package cn.ipanel.apps.dj.hikvision.config;

import cn.ipanel.apps.dj.hikvision.service.MyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Component
public class HikvisionClientConfig {

    private static Logger logger = LoggerFactory.getLogger(HikvisionClientConfig.class);

    private String hikvision_ip;
    private String hikvision_user_name;
    private String hikvision_password;
    private String device;
    @Autowired
    private  SystemConfig config1;
    private static SystemConfig config;


/*    private static String configPath;

    @Value("${hikvision.config.location}")
    public void setConfigPath(String configPath){
        HikvisionClientConfig.configPath=configPath;
    }*/
    @PostConstruct
    public void init() {
        config = config1;
    }

/*    @Autowired
    public HikvisionClientConfig(SystemConfig config){
        HikvisionClientConfig.config=config;
    }*/
    /**
     * 通过配置文件夹路径读取所有指纹机配置
     * @param
     * @return
     */
    public static List<HikvisionClientConfig> getClientConfig(){
        int fileNum = 0;
        logger.info("hikvision.config.location:");
        File file = new File(config.getHikvisionConfigLocation());//File(config.getHikvisionConfigLocation());
        List<HikvisionClientConfig> hikvisionClientConfigs = new ArrayList<>();
        if(file.exists()){
            File[] files = file.listFiles();
            a:for(File file1:files){
                if(file1.isFile()){
                    BufferedReader reader = null;
                    String fileName = null;
                    try {
                        fileName = file1.getAbsolutePath();
                        //加载指纹机配置文件
                        Properties properties = new Properties();
                        reader = new BufferedReader(new FileReader(file1));
                        properties.load(reader);
                        HikvisionClientConfig hikvisionConfig = new HikvisionClientConfig();
                        if(StringUtils.isEmpty(properties.getProperty("device")) || StringUtils.isEmpty(properties.getProperty("hikvision_ip"))
                                || StringUtils.isEmpty(properties.getProperty("hikvision_user_name")) || StringUtils.isEmpty(properties.getProperty("hikvision_password"))) {
                            logger.info("this configuration file: "+fileName+" is invalid");
                            continue a;
                        }
                        hikvisionConfig.setDevice(properties.getProperty("device"));
                        hikvisionConfig.setHikvision_ip(properties.getProperty("hikvision_ip"));
                        hikvisionConfig.setHikvision_user_name(properties.getProperty("hikvision_user_name"));
                        hikvisionConfig.setHikvision_password(properties.getProperty("hikvision_password"));
                        hikvisionClientConfigs.add(hikvisionConfig);
                    }catch (Exception e){
                        logger.error("init hikvisionCondig error: {}", e.getMessage(), e);
                    }finally {
                        try {
                            if(null != reader){
                                //logger.info("finally:"+fileName);
                                reader.close();
                            }
                        }catch (IOException e){
                            logger.info(e.getMessage());
                        }
                    }

                }

            }

        }
        logger.info("the number of Loaded Hikvision client configuration file:  "+hikvisionClientConfigs.size());
        return hikvisionClientConfigs;
    }

    public String getHikvision_ip() {
        return hikvision_ip;
    }

    public void setHikvision_ip(String hikvision_ip) {
        this.hikvision_ip = hikvision_ip;
    }

    public String getHikvision_user_name() {
        return hikvision_user_name;
    }

    public void setHikvision_user_name(String hikvision_user_name) {
        this.hikvision_user_name = hikvision_user_name;
    }

    public String getHikvision_password() {
        return hikvision_password;
    }

    public void setHikvision_password(String hikvision_password) {
        this.hikvision_password = hikvision_password;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }


}
