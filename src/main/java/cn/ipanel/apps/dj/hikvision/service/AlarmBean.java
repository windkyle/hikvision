package cn.ipanel.apps.dj.hikvision.service;

import cn.ipanel.apps.dj.hikvision.Globals;
import cn.ipanel.apps.dj.hikvision.HCNetSDK;

import java.rmi.registry.LocateRegistry;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class AlarmBean {
    private String card;
    private LocalDateTime time;
    private Long longTime;

    public Long getLongTime() {
        return longTime;
    }

    public void setLongTime(Long longTime) {
        this.longTime = longTime;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public AlarmBean() {
    }

    public AlarmBean(String card, HCNetSDK.NET_DVR_TIME netDvrTime) {
        this.card = card;
        time = LocalDateTime.of(
                netDvrTime.dwYear,
                netDvrTime.dwMonth,
                netDvrTime.dwDay,
                netDvrTime.dwHour,
                netDvrTime.dwMinute,
                netDvrTime.dwSecond
        );
        longTime = time.toEpochSecond(OffsetDateTime.now().getOffset());
    }

    @Override
    public String toString() {
        return "AlarmBean: {" +
                "\"card\":\"" + card + '\'' +
                "\", \"time\":\"" + Globals.formatDateTime(time) +
                "\", \"longTime\":" + longTime +
                '}';
    }
}
