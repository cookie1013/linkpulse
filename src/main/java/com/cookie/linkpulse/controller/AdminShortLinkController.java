package com.cookie.linkpulse.controller;

import com.cookie.linkpulse.common.ApiResponse;
import com.cookie.linkpulse.dto.*;
import com.cookie.linkpulse.service.ShortLinkService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/links")
public class AdminShortLinkController {

    private final ShortLinkService shortLinkService;

    public AdminShortLinkController(ShortLinkService shortLinkService) {
        this.shortLinkService = shortLinkService;
    }

    @PostMapping
    public ApiResponse<ShortLinkResponse> createShortLink(@Valid @RequestBody CreateShortLinkRequest request) {
        return ApiResponse.success(shortLinkService.createShortLink(request));
    }

    @GetMapping
    public ApiResponse<PageResponse<ShortLinkPageItemResponse>> pageQuery(ShortLinkPageQuery query) {
        return ApiResponse.success(shortLinkService.pageQuery(query));
    }

    @PatchMapping("/{id}/disable")
    public ApiResponse<Void> disableShortLink(@PathVariable Long id) {
        shortLinkService.disableShortLink(id);
        return ApiResponse.success(null);
    }
}