package com.cookie.linkpulse.common.constant;

public final class RedisKeyConstants {

    private RedisKeyConstants() {
    }

    public static final String SHORT_LINK_PREFIX = "short_link:";

    public static String shortLinkKey(String shortCode) {
        return SHORT_LINK_PREFIX + shortCode;
    }
}