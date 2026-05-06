package com.cookie.linkpulse.dto;

public class RefererStatsItemResponse {

    private String referer;
    private Long pv;

    public RefererStatsItemResponse() {
    }

    public RefererStatsItemResponse(String referer, Long pv) {
        this.referer = referer;
        this.pv = pv;
    }

    public String getReferer() {
        return referer;
    }

    public Long getPv() {
        return pv;
    }
}