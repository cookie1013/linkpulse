package com.cookie.linkpulse.dto;

public class UserAgentStatsItemResponse {

    private String clientType;
    private Long pv;

    public UserAgentStatsItemResponse() {
    }

    public UserAgentStatsItemResponse(String clientType, Long pv) {
        this.clientType = clientType;
        this.pv = pv;
    }

    public String getClientType() {
        return clientType;
    }

    public Long getPv() {
        return pv;
    }
}