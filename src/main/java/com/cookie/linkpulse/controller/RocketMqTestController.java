package com.cookie.linkpulse.controller;

import com.cookie.linkpulse.dto.ShortLinkAccessEvent;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;
import java.time.LocalDateTime;
import com.cookie.linkpulse.dto.ShortLinkAccessEvent;
import java.util.UUID;
import com.cookie.linkpulse.service.ShortLinkAccessEventProducer;

@Profile("dev")
@RestController
public class RocketMqTestController {

    private final RocketMQTemplate rocketMQTemplate;
    private final ShortLinkAccessEventProducer shortLinkAccessEventProducer;
    public RocketMqTestController(RocketMQTemplate rocketMQTemplate,ShortLinkAccessEventProducer shortLinkAccessEventProducer) {
        this.rocketMQTemplate = rocketMQTemplate;
        this.shortLinkAccessEventProducer = shortLinkAccessEventProducer;
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
        event.setEventId(UUID.randomUUID().toString());
        event.setAccessTimestamp(System.currentTimeMillis());

        rocketMQTemplate.convertAndSend("link-access-topic", event);
        return "mq message sent";
    }
    @GetMapping("/mq-duplicate-test")
    public String mqDuplicateTest() {
        String eventId = UUID.randomUUID().toString();

        ShortLinkAccessEvent event = new ShortLinkAccessEvent();
        event.setEventId(eventId);
        event.setShortLinkId(1L);
        event.setShortCode("773ec2");
        event.setOriginalUrl("https://www.meituan.com");
        event.setClientIp("127.0.0.1");
        event.setUserAgent("duplicate-test");
        event.setReferer("mq-duplicate-test");
        event.setAccessTimestamp(System.currentTimeMillis());

        shortLinkAccessEventProducer.sendAccessEvent(event);
        shortLinkAccessEventProducer.sendAccessEvent(event);

        return "duplicate messages sent, eventId=" + eventId;
    }
}