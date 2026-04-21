package com.cookie.linkpulse.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateShortLinkRequest {

    @NotBlank(message = "originalUrl cannot be blank")
    private String originalUrl;

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }
}