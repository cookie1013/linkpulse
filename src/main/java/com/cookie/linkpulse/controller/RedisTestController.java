package com.cookie.linkpulse.controller;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisTestController {

    private final StringRedisTemplate stringRedisTemplate;

    public RedisTestController(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @GetMapping("/redis-test")
    public String redisTest() {
        stringRedisTemplate.opsForValue().set("hello", "redis");
        return stringRedisTemplate.opsForValue().get("hello");
    }
}