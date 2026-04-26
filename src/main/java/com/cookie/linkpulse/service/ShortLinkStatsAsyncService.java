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
    public void recordAccessEvent(ShortLinkAccessEvent event) {
        ShortLink shortLink = shortLinkRepository.findById(event.getShortLinkId()).orElse(null);
        if (shortLink == null) {
            return;
        }

        LocalDateTime accessTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(event.getAccessTimestamp()),
                ZoneId.systemDefault()
        );

        shortLink.setPv(shortLink.getPv() + 1);
        shortLink.setLastAccessTime(accessTime);
        shortLink.setUpdatedAt(LocalDateTime.now());
        shortLinkRepository.save(shortLink);

        ShortLinkAccessLog accessLog = new ShortLinkAccessLog();
        accessLog.setShortLinkId(event.getShortLinkId());
        accessLog.setShortCode(event.getShortCode());
        accessLog.setOriginalUrl(event.getOriginalUrl());
        accessLog.setClientIp(event.getClientIp());
        accessLog.setUserAgent(event.getUserAgent());
        accessLog.setReferer(event.getReferer());
        accessLog.setAccessTime(accessTime);

        shortLinkAccessLogRepository.save(accessLog);
    }
}