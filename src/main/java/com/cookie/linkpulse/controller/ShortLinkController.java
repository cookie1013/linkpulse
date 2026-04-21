package com.cookie.linkpulse.controller;

import com.cookie.linkpulse.common.ApiResponse;
import com.cookie.linkpulse.dto.CreateShortLinkRequest;
import com.cookie.linkpulse.dto.ShortLinkResponse;
import com.cookie.linkpulse.service.ShortLinkService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/links")
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    public ShortLinkController(ShortLinkService shortLinkService) {
        this.shortLinkService = shortLinkService;
    }

    @PostMapping
    public ApiResponse<ShortLinkResponse> createShortLink(@Valid @RequestBody CreateShortLinkRequest request) {
        ShortLinkResponse response = shortLinkService.createShortLink(request);
        return ApiResponse.success(response);
    }
}