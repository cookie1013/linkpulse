package com.cookie.linkpulse.service.impl;

import com.cookie.linkpulse.dto.CreateShortLinkRequest;
import com.cookie.linkpulse.dto.ShortLinkResponse;
import com.cookie.linkpulse.entity.ShortLink;
import com.cookie.linkpulse.repository.ShortLinkRepository;
import com.cookie.linkpulse.service.ShortLinkService;
import org.springframework.stereotype.Service;

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

    private String generateUniqueShortCode() {
        String shortCode;
        do {
            shortCode = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        } while (shortLinkRepository.existsByShortCode(shortCode));
        return shortCode;
    }
}