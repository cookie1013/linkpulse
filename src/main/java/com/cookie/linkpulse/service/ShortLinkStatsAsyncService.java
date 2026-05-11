package com.cookie.linkpulse.service;

import com.cookie.linkpulse.dto.ShortLinkAccessEvent;
import com.cookie.linkpulse.entity.ShortLink;
import com.cookie.linkpulse.entity.ShortLinkAccessLog;
import com.cookie.linkpulse.repository.ShortLinkAccessLogRepository;
import com.cookie.linkpulse.repository.ShortLinkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import java.util.UUID;

@Service
public class ShortLinkStatsAsyncService {

    private final ShortLinkRepository shortLinkRepository;
    private final ShortLinkAccessLogRepository shortLinkAccessLogRepository;

    public ShortLinkStatsAsyncService(ShortLinkRepository shortLinkRepository,
                                      ShortLinkAccessLogRepository shortLinkAccessLogRepository) {
        this.shortLinkRepository = shortLinkRepository;
        this.shortLinkAccessLogRepository = shortLinkAccessLogRepository;
    }

    @Transactional
    public void handleAccessEvent(ShortLinkAccessEvent event) {
        String eventId = event.getEventId();

        if (eventId == null || eventId.isBlank()) {
            eventId = UUID.randomUUID().toString();
        }

        boolean processed = shortLinkAccessLogRepository.existsByShortLinkIdAndEventId(
                event.getShortLinkId(),
                eventId
        );

        if (processed) {
            System.out.println("Duplicate access event ignored, eventId=" + eventId);
            return;
        }

        ShortLink shortLink = shortLinkRepository.findById(event.getShortLinkId())
                .orElse(null);

        if (shortLink == null) {
            System.out.println("Short link not found when consuming access event, shortLinkId=" + event.getShortLinkId());
            return;
        }

        LocalDateTime accessTime = toLocalDateTime(event.getAccessTimestamp());

        Long currentPv = shortLink.getPv() == null ? 0L : shortLink.getPv();
        shortLink.setPv(currentPv + 1);
        shortLink.setLastAccessTime(accessTime);
        shortLink.setUpdatedAt(LocalDateTime.now());
        shortLinkRepository.save(shortLink);

        ShortLinkAccessLog accessLog = new ShortLinkAccessLog();
        accessLog.setEventId(eventId);
        accessLog.setShortLinkId(event.getShortLinkId());
        accessLog.setShortCode(event.getShortCode());
        accessLog.setOriginalUrl(event.getOriginalUrl());
        accessLog.setClientIp(event.getClientIp());
        accessLog.setUserAgent(event.getUserAgent());
        accessLog.setReferer(event.getReferer());
        accessLog.setAccessTime(accessTime);

        shortLinkAccessLogRepository.save(accessLog);

        System.out.println("Access event consumed, eventId=" + eventId + ", shortCode=" + event.getShortCode());
    }

    private LocalDateTime toLocalDateTime(Long timestamp) {
        if (timestamp == null) {
            return LocalDateTime.now();
        }

        return Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}