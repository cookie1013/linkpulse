package com.cookie.linkpulse.service;

import com.cookie.linkpulse.dto.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface ShortLinkService {

    ShortLinkResponse createShortLink(CreateShortLinkRequest request);

    String getOriginalUrlByShortCode(String shortCode, HttpServletRequest request);

    PageResponse<ShortLinkPageItemResponse> pageQuery(ShortLinkPageQuery query);

    void disableShortLink(Long id);

    ShortLinkStatsDetailResponse getStatsDetail(Long id);

    List<TopShortLinkItemResponse> listTopLinks(Integer limit);
    List<PvTrendItemResponse> getPvTrend(Long id, Integer days);
}