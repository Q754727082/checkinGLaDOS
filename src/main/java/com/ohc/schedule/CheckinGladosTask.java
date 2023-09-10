package com.ohc.schedule;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * @Description:
 * @Author: SilenceOu
 * @Date: 2023/9/10 15:43
 */
@Component
@Slf4j
public class CheckinGladosTask {

    private static final String TOKEN = "72d48f40edeb46debc020ee3d65522a0";

    private static final String COOKIE = "koa:sess=eyJ1c2VySWQiOjIwMTY3OSwiX2V4cGlyZSI6MTcwNDIwNTU1NzIzMSwiX21heEFnZSI6MjU5MjAwMDAwMDB9; koa:sess.sig=CkYw8Wrf1h5HKEz0aZGL0eEXMUs";

    @Autowired
    private RestTemplate restTemplate;

    @Scheduled(cron = "0 0 1,8,15,22 * * ?")
    public void checkinGlados() {
        pushMessage(checkin());
    }


    private void pushMessage(String[] content) {
        String stringContent = String.join("<br>", content);
        log.info("推送消息开始");
        // 设置参数
        JSONObject params = new JSONObject();
        params.put("token", TOKEN);
        params.put("title", content[0]);
        params.put("content", stringContent);
        params.put("template", "markdown");
        params.put("channel", "wechat");
        // 请求url
        String url = "https://www.pushplus.plus/send";
        Map result = restTemplate.postForObject(url, params, Map.class);
        log.info("pushplus返回结果: {}", result);
        log.info("推送消息结束");
    }

    private String[] checkin() {
        try {
            log.info("签到glados开始");
            // 设置请求头
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("cookie", COOKIE);
            httpHeaders.add("referer", "https://glados.rocks/console/checkin");
            httpHeaders.add("user-agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0)");
            // 设置参数
            JSONObject params = new JSONObject();
            params.put("token", "glados.one");
            // 组装
            HttpEntity<JSONObject> checkinRequest = new HttpEntity<>(params, httpHeaders);
            // 请求url
            String url = "https://glados.rocks/api/user/checkin";
            Map checkin = restTemplate.postForObject(url, checkinRequest, Map.class);
            String message = (String) checkin.get("message");
            log.info("glados签到返回结果: {}", checkin);
            log.info("签到glados结束");

            // 获取签到状态
            log.info("获取签到状态开始");
            // 组装
            HttpEntity<JSONObject> statusRequest = new HttpEntity<>(null, httpHeaders);
            // 请求url
            String getStatusUrl = "https://glados.rocks/api/user/status";
            ResponseEntity<Map> responseEntity = restTemplate.exchange(getStatusUrl, HttpMethod.GET, statusRequest, Map.class);
            Map status = responseEntity.getBody();
            Map data = (Map) status.get("data");
            Integer leftDays = (Integer) data.get("leftDays");
            String days = Integer.toString(leftDays);
            log.info("glados签到状态返回结果: {}", status);
            log.info("获取签到状态结束");
            String[] successResult = {"签到成功", message, "剩余天数:" + days};
            return successResult;
        } catch (Exception e) {
            String[] errorResult = {"签到失败", e.getMessage()};
            return errorResult;
        }
    }
}
