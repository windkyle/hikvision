package cn.ipanel.apps.dj.hikvision.controller;

import cn.ipanel.apps.dj.hikvision.task.ExportBean;
import cn.ipanel.apps.dj.hikvision.task.ExportResultBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ThreadLocalRandom;

@RestController
public class TestController {


    private static Logger logger = LoggerFactory.getLogger(TestController.class);

    @RequestMapping(value = "export")
    public ExportResultBean export(@RequestBody ExportBean bean) {
        logger.info("receive new export: {}", bean.toString());
        ExportResultBean resultBean = new ExportResultBean();
        int code = ThreadLocalRandom.current().nextInt(10);
        if (code > 1) {
            resultBean.setResult(ExportResultBean.successResult);
        } else {
            resultBean.setResult(ExportResultBean.failedResult);
            resultBean.setMsg("something rong");
        }
        return resultBean;
    }
}
