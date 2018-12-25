package cn.ipanel.apps.dj.hikvision.task;

public class ExportResultBean {
    public static final String successResult = "ok";
    public static final String failedResult = "fail";
    private String result;
    private String msg;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public ExportResultBean() {
    }
}
