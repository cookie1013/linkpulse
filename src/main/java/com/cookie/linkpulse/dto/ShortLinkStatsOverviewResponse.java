package com.cookie.linkpulse.dto;

import java.time.LocalDateTime;

public class ShortLinkStatsOverviewResponse {

    private Long id;
    private String shortCode;
    private String originalUrl;
    private String shortUrl;
    private Long totalPv;
    private Long todayPv;
    private Long yesterdayPv;
    private Long last7DaysPv;
    private Long uniqueIpCount;
    private LocalDateTime lastAccessTime;

    public ShortLinkStatsOverviewResponse() {
    }

    public ShortLinkStatsOverviewResponse(Long id,
                                          String shortCode,
                                          String originalUrl,
                                          String shortUrl,
                                          Long totalPv,
                                          Long todayPv,
                                          Long yesterdayPv,
                                          Long last7DaysPv,
                                          Long uniqueIpCount,
                                          LocalDateTime lastAccessTime) {
        this.id = id;
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.shortUrl = shortUrl;
        this.totalPv = totalPv;
        this.todayPv = todayPv;
        this.yesterdayPv = yesterdayPv;
        this.last7DaysPv = last7DaysPv;
        this.uniqueIpCount = uniqueIpCount;
        this.lastAccessTime = lastAccessTime;
    }

    public Long getId() {
        return id;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public Long getTotalPv() {
        return totalPv;
    }

    public Long getTodayPv() {
        return todayPv;
    }

    public Long getYesterdayPv() {
        return yesterdayPv;
    }

    public Long getLast7DaysPv() {
        return last7DaysPv;
    }

    public Long getUniqueIpCount() {
        return uniqueIpCount;
    }

    public LocalDateTime getLastAccessTime() {
        return lastAccessTime;
    }
}