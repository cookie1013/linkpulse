package com.cookie.linkpulse.service;

import com.cookie.linkpulse.dto.ShortLinkAccessEvent;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Service;
import com.cookie.linkpulse.common.constant.MqConstants;
@Service
public class ShortLinkAccessEventProducer {

    private final RocketMQTemplate rocketMQTemplate;

    public ShortLinkAccessEventProducer(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    public void sendAccessEvent(ShortLinkAccessEvent event) {
        rocketMQTemplate.convertAndSend(MqConstants.LINK_ACCESS_TOPIC, event);
    }
}