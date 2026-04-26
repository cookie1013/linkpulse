package com.cookie.linkpulse.service.impl;
import com.cookie.linkpulse.dto.*;
import com.cookie.linkpulse.service.ShortLinkAccessEventProducer;
import org.springframework.transaction.annotation.Transactional;
import com.cookie.linkpulse.entity.ShortLink;
import com.cookie.linkpulse.repository.ShortLinkRepository;
import com.cookie.linkpulse.service.ShortLinkService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;
import com.cookie.linkpulse.entity.ShortLinkAccessLog;
import com.cookie.linkpulse.repository.ShortLinkAccessLogRepository;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
@Service
public class ShortLinkServiceImpl implements ShortLinkService {

    private final ShortLinkRepository shortLinkRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final ShortLinkAccessLogRepository shortLinkAccessLogRepository;
    private final ShortLinkAccessEventProducer shortLinkAccessEventProducer;

    public ShortLinkServiceImpl(ShortLinkRepository shortLinkRepository,
                                StringRedisTemplate stringRedisTemplate,
                                ShortLinkAccessLogRepository shortLinkAccessLogRepository,
                                ShortLinkAccessEventProducer shortLinkAccessEventProducer) {
        this.shortLinkRepository = shortLinkRepository;
        this.stringRedisTemplate = stringRedisTemplate;
        this.shortLinkAccessLogRepository = shortLinkAccessLogRepository;
        this.shortLinkAccessEventProducer = shortLinkAccessEventProducer;
    }

    @Override
    public ShortLinkResponse createShortLink(CreateShortLinkRequest request) {
        String shortCode = generateUniqueShortCode();

        ShortLink shortLink = new ShortLink();
        shortLink.setShortCode(shortCode);
        shortLink.setOriginalUrl(request.getOriginalUrl());
        shortLink.setStatus(1);
        shortLink.setPv(0L);
        shortLink.setLastAccessTime(null);
        shortLink.setExpireTime(request.getExpireTime());
        shortLink.setCreatedAt(LocalDateTime.now());
        shortLink.setUpdatedAt(LocalDateTime.now());

        ShortLink saved = shortLinkRepository.save(shortLink);

        return new ShortLinkResponse(
                saved.getId(),
                saved.getShortCode(),
                saved.getOriginalUrl(),
                "http://localhost:8080/" + saved.getShortCode()
        );
    }
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
    @Transactional(readOnly = true)
    @Override
    public String getOriginalUrlByShortCode(String shortCode, HttpServletRequest request) {
        ShortLink shortLink = shortLinkRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "short link not found"));

        if (shortLink.getStatus() != 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "short link is disabled");
        }

        if (shortLink.getExpireTime() != null && shortLink.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "short link is expired");
        }

        String redisKey = "short_link:" + shortCode;
        String cachedOriginalUrl = stringRedisTemplate.opsForValue().get(redisKey);

        String originalUrl;
        if (cachedOriginalUrl != null && !cachedOriginalUrl.isEmpty()) {
            System.out.println("Redis hit for shortCode: " + shortCode);
            originalUrl = cachedOriginalUrl;
        } else {
            System.out.println("Redis miss for shortCode: " + shortCode);
            originalUrl = shortLink.getOriginalUrl();
            stringRedisTemplate.opsForValue().set(redisKey, originalUrl);
        }

        ShortLinkAccessEvent event = new ShortLinkAccessEvent();
        event.setShortLinkId(shortLink.getId());
        event.setShortCode(shortLink.getShortCode());
        event.setOriginalUrl(shortLink.getOriginalUrl());
        event.setClientIp(getClientIp(request));
        event.setUserAgent(request.getHeader("User-Agent"));
        event.setReferer(request.getHeader("Referer"));
        event.setAccessTimestamp(System.currentTimeMillis());

        shortLinkAccessEventProducer.sendAccessEvent(event);

        return originalUrl;
    }

    @Override
    public ShortLinkStatsDetailResponse getStatsDetail(Long id) {
        ShortLink shortLink = shortLinkRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "short link not found"));

        List<AccessLogItemResponse> recentLogs = shortLinkAccessLogRepository
                .findTop10ByShortLinkIdOrderByAccessTimeDesc(id)
                .stream()
                .map(log -> new AccessLogItemResponse(
                        log.getClientIp(),
                        log.getUserAgent(),
                        log.getReferer(),
                        log.getAccessTime()
                ))
                .toList();

        return new ShortLinkStatsDetailResponse(
                shortLink.getId(),
                shortLink.getShortCode(),
                shortLink.getOriginalUrl(),
                "http://localhost:8080/" + shortLink.getShortCode(),
                shortLink.getStatus(),
                shortLink.getPv(),
                shortLink.getLastAccessTime(),
                shortLink.getExpireTime(),
                recentLogs
        );
    }
    @Override
    public List<TopShortLinkItemResponse> listTopLinks(Integer limit) {
        int size = (limit == null || limit < 1) ? 5 : limit;

        PageRequest pageRequest = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "pv"));
        Page<ShortLink> page = shortLinkRepository.findByStatus(1, pageRequest);

        return page.getContent().stream()
                .map(item -> new TopShortLinkItemResponse(
                        item.getId(),
                        item.getShortCode(),
                        item.getOriginalUrl(),
                        "http://localhost:8080/" + item.getShortCode(),
                        item.getPv()
                ))
                .toList();
    }
    private String generateUniqueShortCode() {
        String shortCode;
        do {
            shortCode = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        } while (shortLinkRepository.existsByShortCode(shortCode));
        return shortCode;
    }

    @Override
    public PageResponse<ShortLinkPageItemResponse> pageQuery(ShortLinkPageQuery query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();

        String shortCode = query.getShortCode() == null ? "" : query.getShortCode();
        String originalUrl = query.getOriginalUrl() == null ? "" : query.getOriginalUrl();

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

        Page<ShortLink> page = shortLinkRepository.findByShortCodeContainingAndOriginalUrlContaining(
                shortCode, originalUrl, pageable
        );

        List<ShortLinkPageItemResponse> records = page.getContent().stream()
                .map(item -> new ShortLinkPageItemResponse(
                        item.getId(),
                        item.getShortCode(),
                        item.getOriginalUrl(),
                        "http://localhost:8080/" + item.getShortCode(),
                        item.getStatus(),
                        item.getPv(),
                        item.getLastAccessTime(),
                        item.getExpireTime()
                ))
                .toList();

        return new PageResponse<>(records, page.getTotalElements(), pageNum, pageSize);
    }
    @Transactional
    @Override
    public void disableShortLink(Long id) {
        ShortLink shortLink = shortLinkRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "short link not found"));

        shortLink.setStatus(0);
        shortLink.setUpdatedAt(LocalDateTime.now());
        shortLinkRepository.save(shortLink);

        String redisKey = "short_link:" + shortLink.getShortCode();
        stringRedisTemplate.delete(redisKey);
    }
}