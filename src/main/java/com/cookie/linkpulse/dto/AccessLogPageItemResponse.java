package com.cookie.linkpulse.dto;

import java.time.LocalDateTime;

public class AccessLogPageItemResponse {

    private Long id;
    private Long shortLinkId;
    private String shortCode;
    private String clientIp;
    private String userAgent;
    private String referer;
    private LocalDateTime accessTime;

    public AccessLogPageItemResponse() {
    }

    public AccessLogPageItemResponse(Long id,
                                     Long shortLinkId,
                                     String shortCode,
                                     String clientIp,
                                     String userAgent,
                                     String referer,
                                     LocalDateTime accessTime) {
        this.id = id;
        this.shortLinkId = shortLinkId;
        this.shortCode = shortCode;
        this.clientIp = clientIp;
        this.userAgent = userAgent;
        this.referer = referer;
        this.accessTime = accessTime;
    }

    public Long getId() {
        return id;
    }

    public Long getShortLinkId() {
        return shortLinkId;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getClientIp() {
        return clientIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getReferer() {
        return referer;
    }

    public LocalDateTime getAccessTime() {
        return accessTime;
    }
}