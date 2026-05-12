package com.cookie.linkpulse.common.util;

import com.cookie.linkpulse.common.constant.HttpHeaderConstants;
import jakarta.servlet.http.HttpServletRequest;

public final class IpUtils {

    private IpUtils() {
    }

    public static String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader(HttpHeaderConstants.X_FORWARDED_FOR);

        if (xForwardedFor != null
                && !xForwardedFor.isBlank()
                && !HttpHeaderConstants.UNKNOWN.equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}