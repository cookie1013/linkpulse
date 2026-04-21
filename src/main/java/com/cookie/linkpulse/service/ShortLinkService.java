package com.cookie.linkpulse.service;

import com.cookie.linkpulse.dto.CreateShortLinkRequest;
import com.cookie.linkpulse.dto.ShortLinkResponse;

public interface ShortLinkService {

    ShortLinkResponse createShortLink(CreateShortLinkRequest request);
}