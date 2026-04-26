package com.cookie.linkpulse.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serializable;

public class ShortLinkAccessEvent implements Serializable {

    private Long shortLinkId;
    private String shortCode;
    private String originalUrl;
    private String clientIp;
    private String userAgent;
    private String referer;
    private Long accessTimestamp;

    public ShortLinkAccessEvent() {
    }

    public Long getShortLinkId() {
        return shortLinkId;
    }

    public void setShortLinkId(Long shortLinkId) {
        this.shortLinkId = shortLinkId;
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

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public Long getAccessTimestamp() {
        return accessTimestamp;
    }

    public void setAccessTimestamp(Long accessTimestamp) {
        this.accessTimestamp = accessTimestamp;
    }
}