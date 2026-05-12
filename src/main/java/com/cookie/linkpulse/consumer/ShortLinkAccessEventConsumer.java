package com.cookie.linkpulse.consumer;

import com.cookie.linkpulse.dto.ShortLinkAccessEvent;
import com.cookie.linkpulse.service.ShortLinkStatsAsyncService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;
import com.cookie.linkpulse.common.constant.MqConstants;
@Service
@RocketMQMessageListener(
        topic = MqConstants.LINK_ACCESS_TOPIC,
        consumerGroup = MqConstants.LINK_ACCESS_EVENT_CONSUMER_GROUP
)
public class ShortLinkAccessEventConsumer implements RocketMQListener<ShortLinkAccessEvent> {

    private final ShortLinkStatsAsyncService shortLinkStatsAsyncService;

    public ShortLinkAccessEventConsumer(ShortLinkStatsAsyncService shortLinkStatsAsyncService) {
        this.shortLinkStatsAsyncService = shortLinkStatsAsyncService;
    }

    @Override
    public void onMessage(ShortLinkAccessEvent message) {
        shortLinkStatsAsyncService.handleAccessEvent(message);
    }
}
