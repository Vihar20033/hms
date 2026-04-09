package com.hms.common.concurrency.impl;

import com.hms.common.concurrency.CounterService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "hms.redis.enabled", havingValue = "true")
public class RedisCounterService implements CounterService {

    private final StringRedisTemplate redisTemplate;

    @Override
    public long increment(String key) {
        Long value = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofDays(2));
        return value != null ? value : 1L;
    }
}
