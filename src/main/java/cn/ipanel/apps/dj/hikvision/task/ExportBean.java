package cn.ipanel.apps.dj.hikvision.task;

public class ExportBean {
    private String card;
    private Long time;
    private String device;

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public ExportBean(String card, Long time, String device) {
        this.card = card;
        this.time = time;
        this.device = device;
    }

    public ExportBean() {
    }

    @Override
    public String toString() {
        return "ExportBean{" +
                "card='" + card + '\'' +
                ", time=" + time +
                ", device='" + device + '\'' +
                '}';
    }
}
