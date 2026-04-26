package com.cookie.linkpulse.service;

import com.cookie.linkpulse.dto.ShortLinkAccessEvent;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Service;

@Service
public class ShortLinkAccessEventProducer {

    private final RocketMQTemplate rocketMQTemplate;

    public ShortLinkAccessEventProducer(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    public void sendAccessEvent(ShortLinkAccessEvent event) {
        rocketMQTemplate.convertAndSend("link-access-topic", event);
    }
}