package com.cookie.linkpulse.controller;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.cookie.linkpulse.common.ApiResponse;
import com.cookie.linkpulse.dto.*;
import com.cookie.linkpulse.service.ShortLinkService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/links")
public class AdminShortLinkController {

    private final ShortLinkService shortLinkService;

    public AdminShortLinkController(ShortLinkService shortLinkService) {
        this.shortLinkService = shortLinkService;
    }

    @PostMapping
    public ApiResponse<ShortLinkResponse> createShortLink(@Valid @RequestBody CreateShortLinkRequest request) {
        Entry entry = null;

        try {
            entry = SphU.entry("admin-create-short-link");

            return ApiResponse.success(shortLinkService.createShortLink(request));

        } catch (BlockException ex) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "too many requests, please try again later"
            );

        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
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