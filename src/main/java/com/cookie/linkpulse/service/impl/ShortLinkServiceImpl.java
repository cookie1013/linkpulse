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

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ShortLinkServiceImpl implements ShortLinkService {

    private final ShortLinkRepository shortLinkRepository;

    public ShortLinkServiceImpl(ShortLinkRepository shortLinkRepository) {
        this.shortLinkRepository = shortLinkRepository;
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
                .findByShortCodeAndStatus(shortCode, 1)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "short link not found"));

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
}