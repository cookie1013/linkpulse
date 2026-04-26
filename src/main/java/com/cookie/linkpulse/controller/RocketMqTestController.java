package com.cookie.linkpulse.controller;

import com.cookie.linkpulse.dto.ShortLinkAccessEvent;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class RocketMqTestController {

    private final RocketMQTemplate rocketMQTemplate;

    public RocketMqTestController(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    @GetMapping("/mq-test")
    public String mqTest() {
        ShortLinkAccessEvent event = new ShortLinkAccessEvent();
        event.setShortLinkId(1L);
        event.setShortCode("test123");
        event.setOriginalUrl("https://www.meituan.com");
        event.setClientIp("127.0.0.1");
        event.setUserAgent("Postman");
        event.setReferer("local-test");
        event.setAccessTimestamp(System.currentTimeMillis());

        rocketMQTemplate.convertAndSend("link-access-topic", event);
        return "mq message sent";
    }
}