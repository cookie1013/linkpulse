package com.cookie.linkpulse.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class CreateShortLinkRequest {

    @NotBlank(message = "originalUrl cannot be blank")
    private String originalUrl;

    private LocalDateTime expireTime;

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }
}