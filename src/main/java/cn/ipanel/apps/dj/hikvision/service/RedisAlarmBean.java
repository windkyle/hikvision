package cn.ipanel.apps.dj.hikvision.service;

public class RedisAlarmBean extends AlarmBean {
    private Boolean hasExprotToExternalSystem;
    private Long exportToExternalSystemTime;
/*    private String device;

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }*/

    public Boolean getHasExprotToExternalSystem() {
        return hasExprotToExternalSystem;
    }

    public void setHasExprotToExternalSystem(Boolean hasExprotToExternalSystem) {
        this.hasExprotToExternalSystem = hasExprotToExternalSystem;
    }

    public Long getExportToExternalSystemTime() {
        return exportToExternalSystemTime;
    }

    public void setExportToExternalSystemTime(Long exportToExternalSystemTime) {
        this.exportToExternalSystemTime = exportToExternalSystemTime;
    }

    public RedisAlarmBean(AlarmBean bean) {
        super.setCard(bean.getCard());
        super.setTime(bean.getTime());
        super.setLongTime(bean.getLongTime());
        super.setDevice(bean.getDevice());
        this.hasExprotToExternalSystem = false;
        //this.device = device;
    }
}
