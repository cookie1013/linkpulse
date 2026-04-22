package com.cookie.linkpulse.dto;

import java.time.LocalDateTime;

public class ShortLinkPageItemResponse {

    private Long id;
    private String shortCode;
    private String originalUrl;
    private String shortUrl;
    private Integer status;
    private Long pv;
    private LocalDateTime lastAccessTime;
    private LocalDateTime expireTime;

    public ShortLinkPageItemResponse() {
    }

    public ShortLinkPageItemResponse(Long id, String shortCode, String originalUrl,
                                     String shortUrl, Integer status, Long pv,
                                     LocalDateTime lastAccessTime, LocalDateTime expireTime) {
        this.id = id;
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.shortUrl = shortUrl;
        this.status = status;
        this.pv = pv;
        this.lastAccessTime = lastAccessTime;
        this.expireTime = expireTime;
    }

    public Long getId() {
        return id;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public Integer getStatus() {
        return status;
    }

    public Long getPv() {
        return pv;
    }

    public LocalDateTime getLastAccessTime() {
        return lastAccessTime;
    }
    public LocalDateTime getExpireTime() {
        return expireTime;
    }
}