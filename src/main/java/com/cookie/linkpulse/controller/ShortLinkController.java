package com.cookie.linkpulse.controller;

import com.cookie.linkpulse.common.ApiResponse;
import com.cookie.linkpulse.dto.CreateShortLinkRequest;
import com.cookie.linkpulse.dto.ShortLinkResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/links")
public class ShortLinkController {

    @PostMapping
    public ApiResponse<ShortLinkResponse> createShortLink(@Valid @RequestBody CreateShortLinkRequest request) {
        ShortLinkResponse response = new ShortLinkResponse(
                1L,
                "abc123",
                request.getOriginalUrl(),
                "http://localhost:8080/abc123"
        );
        return ApiResponse.success(response);
    }
}
