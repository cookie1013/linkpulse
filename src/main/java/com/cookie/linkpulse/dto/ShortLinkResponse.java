package com.cookie.linkpulse.dto;

public class ShortLinkResponse {

    private Long id;
    private String shortCode;
    private String originalUrl;
    private String shortUrl;

    public ShortLinkResponse() {
    }

    public ShortLinkResponse(Long id, String shortCode, String originalUrl, String shortUrl) {
        this.id = id;
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.shortUrl = shortUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }
}