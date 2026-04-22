package com.cookie.linkpulse.dto;

import java.time.LocalDateTime;

public class AccessLogItemResponse {

    private String clientIp;
    private String userAgent;
    private String referer;
    private LocalDateTime accessTime;

    public AccessLogItemResponse() {
    }

    public AccessLogItemResponse(String clientIp, String userAgent, String referer, LocalDateTime accessTime) {
        this.clientIp = clientIp;
        this.userAgent = userAgent;
        this.referer = referer;
        this.accessTime = accessTime;
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