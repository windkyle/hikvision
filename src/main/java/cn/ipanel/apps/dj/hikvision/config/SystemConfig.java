package cn.ipanel.apps.dj.hikvision.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SystemConfig {
    @Value("${hikvision.service.ip}")
    private String m_sDeviceIP;
    @Value("${hikvision.service.username}")
    private String m_sUserName;
    @Value("${hikvision.service.password}")
    private String m_sPassword;
    @Value("${export.url}")
    private String exportUrl;
    @Value("${hikvision.device}")
    private String device;
    @Value("${hikvision.writeLog}")
    private String writeLog;
    @Value("${data.save.path}")
    private String dataSavePath;
    @Value("${runtime}")
    private Integer runtime;
    @Value("${hikvision.config.location}")
    private String hikvisionConfigLocation;

    public Integer getRuntime() {
        return runtime;
    }

    public void setRuntime(Integer runtime) {
        this.runtime = runtime;
    }

    public String getDataSavePath() {
        return dataSavePath;
    }

    public void setDataSavePath(String dataSavePath) {
        this.dataSavePath = dataSavePath;
    }

    public String getWriteLog() {
        return writeLog;
    }

    public void setWriteLog(String writeLog) {
        this.writeLog = writeLog;
    }

    public String getM_sDeviceIP() {
        return m_sDeviceIP;
    }

    public void setM_sDeviceIP(String m_sDeviceIP) {
        this.m_sDeviceIP = m_sDeviceIP;
    }

    public String getM_sUserName() {
        return m_sUserName;
    }

    public void setM_sUserName(String m_sUserName) {
        this.m_sUserName = m_sUserName;
    }

    public String getM_sPassword() {
        return m_sPassword;
    }

    public void setM_sPassword(String m_sPassword) {
        this.m_sPassword = m_sPassword;
    }

    public String getExportUrl() {
        return exportUrl;
    }

    public void setExportUrl(String exportUrl) {
        this.exportUrl = exportUrl;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public SystemConfig() {
    }
    public String getHikvisionConfigLocation() {
        return hikvisionConfigLocation;
    }

    public void setHikvisionConfigLocation(String hikvisionConfigLocation) {
        this.hikvisionConfigLocation = hikvisionConfigLocation;
    }

}
