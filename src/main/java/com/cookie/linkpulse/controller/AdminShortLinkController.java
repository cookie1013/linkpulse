package com.cookie.linkpulse.controller;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.cookie.linkpulse.common.ApiResponse;
import com.cookie.linkpulse.dto.CreateShortLinkRequest;
import com.cookie.linkpulse.dto.PageResponse;
import com.cookie.linkpulse.dto.ShortLinkPageItemResponse;
import com.cookie.linkpulse.dto.ShortLinkPageQuery;
import com.cookie.linkpulse.dto.ShortLinkResponse;
import com.cookie.linkpulse.service.ShortLinkService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.cookie.linkpulse.common.constant.SentinelResourceConstants;
@Validated
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
            entry = SphU.entry(SentinelResourceConstants.ADMIN_CREATE_SHORT_LINK);

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
    public ApiResponse<PageResponse<ShortLinkPageItemResponse>> pageQuery(@Valid ShortLinkPageQuery query) {
        return ApiResponse.success(shortLinkService.pageQuery(query));
    }

    @PatchMapping("/{id}/disable")
    public ApiResponse<Void> disableShortLink(
            @PathVariable
            @Min(value = 1, message = "id must be greater than or equal to 1")
            Long id) {
        shortLinkService.disableShortLink(id);
        return ApiResponse.success(null);
    }
}