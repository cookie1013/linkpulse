package com.cookie.linkpulse.dto;

public class TopShortLinkItemResponse {

    private Long id;
    private String shortCode;
    private String originalUrl;
    private String shortUrl;
    private Long pv;

    public TopShortLinkItemResponse() {
    }

    public TopShortLinkItemResponse(Long id, String shortCode, String originalUrl, String shortUrl, Long pv) {
        this.id = id;
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.shortUrl = shortUrl;
        this.pv = pv;
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

    public Long getPv() {
        return pv;
    }
}