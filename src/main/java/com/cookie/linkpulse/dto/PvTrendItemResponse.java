package com.cookie.linkpulse.dto;

public class PvTrendItemResponse {

    private String date;
    private Long pv;

    public PvTrendItemResponse() {
    }

    public PvTrendItemResponse(String date, Long pv) {
        this.date = date;
        this.pv = pv;
    }

    public String getDate() {
        return date;
    }

    public Long getPv() {
        return pv;
    }
}