package com.hms.security.service;

import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Value("${hms.rate-limit.auth.capacity:10}")
    private int capacity;

    @Value("${hms.rate-limit.auth.refill-minutes:1}")
    private int refillMinutes;

    // If Bucket exist  -> Return it
    public Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, this::newBucket);
    }

    // Create a new Bucket
    private Bucket newBucket(String key) {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(capacity).refillIntervally(capacity, Duration.ofMinutes(refillMinutes)))
                .build();
    }
}
