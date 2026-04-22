package com.cookie.linkpulse.service.impl;
import org.springframework.transaction.annotation.Transactional;
import com.cookie.linkpulse.dto.CreateShortLinkRequest;
import com.cookie.linkpulse.dto.ShortLinkResponse;
import com.cookie.linkpulse.entity.ShortLink;
import com.cookie.linkpulse.repository.ShortLinkRepository;
import com.cookie.linkpulse.service.ShortLinkService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.UUID;
import com.cookie.linkpulse.dto.PageResponse;
import com.cookie.linkpulse.dto.ShortLinkPageItemResponse;
import com.cookie.linkpulse.dto.ShortLinkPageQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;
@Service
public class ShortLinkServiceImpl implements ShortLinkService {

    private final ShortLinkRepository shortLinkRepository;
    private final StringRedisTemplate stringRedisTemplate;

    public ShortLinkServiceImpl(ShortLinkRepository shortLinkRepository,
                                StringRedisTemplate stringRedisTemplate) {
        this.shortLinkRepository = shortLinkRepository;
        this.stringRedisTemplate = stringRedisTemplate;
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

    @Transactional
    @Override
    public String getOriginalUrlByShortCode(String shortCode) {
        ShortLink shortLink = shortLinkRepository
                .findByShortCode(shortCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "short link not found"));

        if (shortLink.getStatus() != 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "short link is disabled");
        }

        if (shortLink.getExpireTime() != null && shortLink.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "short link is expired");
        }

        String redisKey = "short_link:" + shortCode;
        String cachedOriginalUrl = stringRedisTemplate.opsForValue().get(redisKey);

        if (cachedOriginalUrl != null && !cachedOriginalUrl.isEmpty()) {
            System.out.println("Redis hit for shortCode: " + shortCode);

            shortLink.setPv(shortLink.getPv() + 1);
            shortLink.setLastAccessTime(LocalDateTime.now());
            shortLink.setUpdatedAt(LocalDateTime.now());
            shortLinkRepository.save(shortLink);

            return cachedOriginalUrl;
        }

        System.out.println("Redis miss for shortCode: " + shortCode);

        stringRedisTemplate.opsForValue().set(redisKey, shortLink.getOriginalUrl());

        shortLink.setPv(shortLink.getPv() + 1);
        shortLink.setLastAccessTime(LocalDateTime.now());
        shortLink.setUpdatedAt(LocalDateTime.now());
        shortLinkRepository.save(shortLink);

        return shortLink.getOriginalUrl();
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