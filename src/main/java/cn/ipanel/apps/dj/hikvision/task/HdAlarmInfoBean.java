package cn.ipanel.apps.dj.hikvision.task;

import cn.ipanel.apps.dj.hikvision.Globals;
import cn.ipanel.apps.dj.hikvision.service.RedisAlarmBean;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class HdAlarmInfoBean {
    private String card;
    private String time;
    private Long longTime;
    private String exportTime;
    private Long longExportTime;

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Long getLongTime() {
        return longTime;
    }

    public void setLongTime(Long longTime) {
        this.longTime = longTime;
    }

    public String getExportTime() {
        return exportTime;
    }

    public void setExportTime(String exportTime) {
        this.exportTime = exportTime;
    }

    public Long getLongExportTime() {
        return longExportTime;
    }

    public void setLongExportTime(Long longExportTime) {
        this.longExportTime = longExportTime;
    }

    public HdAlarmInfoBean(String card, String time, Long longTime, String exportTime, Long longExportTime) {
        this.card = card;
        this.time = time;
        this.longTime = longTime;
        this.exportTime = exportTime;
        this.longExportTime = longExportTime;
    }

    public HdAlarmInfoBean(RedisAlarmBean bean) {
        this.card = bean.getCard();
        this.time = Globals.formatDateTime(bean.getTime());
        this.longTime = bean.getLongTime();
        this.exportTime = Globals.formatDateTime(LocalDateTime.ofInstant(Instant.ofEpochSecond(bean.getExportToExternalSystemTime()), ZoneId.systemDefault()));
        this.longExportTime = bean.getExportToExternalSystemTime();
    }

    public HdAlarmInfoBean() {
    }
}
