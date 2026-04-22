package com.cookie.linkpulse.service;

import com.cookie.linkpulse.dto.CreateShortLinkRequest;
import com.cookie.linkpulse.dto.ShortLinkResponse;
import com.cookie.linkpulse.dto.PageResponse;
import com.cookie.linkpulse.dto.ShortLinkPageItemResponse;
import com.cookie.linkpulse.dto.ShortLinkPageQuery;


public interface ShortLinkService {

    ShortLinkResponse createShortLink(CreateShortLinkRequest request);

    String getOriginalUrlByShortCode(String shortCode);
    PageResponse<ShortLinkPageItemResponse> pageQuery(ShortLinkPageQuery query);
    void disableShortLink(Long id);
}