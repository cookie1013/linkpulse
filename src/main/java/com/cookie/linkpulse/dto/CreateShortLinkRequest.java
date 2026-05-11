package com.cookie.linkpulse.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

public class CreateShortLinkRequest {

    @NotBlank(message = "originalUrl cannot be blank")
    @URL(message = "originalUrl must be a valid URL")
    private String originalUrl;

    @Future(message = "expireTime must be a future time")
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