package cn.ipanel.apps.dj.hikvision.config;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HikvisionClientConfig {
    private String hikvision_ip;
    private String hikvision_user_name;
    private String hikvision_password;
    private String device;

    /**
     * 通过配置文件夹路径读取所有指纹机配置
     * @param path
     * @return
     */
    public List<HikvisionClientConfig> getClientConfig(String path){
        return null;
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
