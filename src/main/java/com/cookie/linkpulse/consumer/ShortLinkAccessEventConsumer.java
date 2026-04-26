package com.cookie.linkpulse.consumer;

import com.cookie.linkpulse.dto.ShortLinkAccessEvent;
import com.cookie.linkpulse.service.ShortLinkStatsAsyncService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

@Service
@RocketMQMessageListener(
        topic = "link-access-topic",
        consumerGroup = "linkpulse-access-event-consumer"
)
public class ShortLinkAccessEventConsumer implements RocketMQListener<ShortLinkAccessEvent> {

    private final ShortLinkStatsAsyncService shortLinkStatsAsyncService;

    public ShortLinkAccessEventConsumer(ShortLinkStatsAsyncService shortLinkStatsAsyncService) {
        this.shortLinkStatsAsyncService = shortLinkStatsAsyncService;
    }

    @Override
    public void onMessage(ShortLinkAccessEvent message) {
        System.out.println("RocketMQ consumer received message: shortCode="
                + message.getShortCode() + ", accessTimestamp=" + message.getAccessTimestamp());

        shortLinkStatsAsyncService.recordAccessEvent(message);
    }
}