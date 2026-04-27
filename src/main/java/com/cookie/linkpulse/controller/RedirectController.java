package com.cookie.linkpulse.controller;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.cookie.linkpulse.service.ShortLinkService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
public class RedirectController {

    private final ShortLinkService shortLinkService;

    public RedirectController(ShortLinkService shortLinkService) {
        this.shortLinkService = shortLinkService;
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode, HttpServletRequest request) {
        Entry entry = null;

        try {
            entry = SphU.entry("short-link-redirect");

            String originalUrl = shortLinkService.getOriginalUrlByShortCode(shortCode, request);

            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(originalUrl));

            return new ResponseEntity<>(headers, HttpStatus.FOUND);

        } catch (BlockException ex) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();

        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }
}