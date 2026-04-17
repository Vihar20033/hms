package com.hms.security.idempotency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hms.common.exception.BadRequestException;
import com.hms.common.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private static final String REDIS_PREFIX = "idempotency";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_DONE = "DONE";
    private static final Duration PENDING_TTL = Duration.ofMinutes(2);
    private static final Duration DONE_TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public <T> T execute(
            String idempotencyKey,
            String operation,
            String userScope,
            String fingerprint,
            Supplier<T> action,
            Function<T, Long> responseIdExtractor,
            Function<Long, T> replayLoader
    ) {
        String normalizedKey = normalizeIdempotencyKey(idempotencyKey);
        String redisKey = buildRedisKey(operation, userScope, normalizedKey);

        String existing = redisTemplate.opsForValue().get(redisKey);
        if (existing != null) {
            return resolveExistingRecord(existing, fingerprint, replayLoader);
        }

        boolean locked = Boolean.TRUE.equals(
                redisTemplate.opsForValue().setIfAbsent(redisKey, encodePending(fingerprint), PENDING_TTL)
        );

        if (!locked) {
            String concurrentValue = redisTemplate.opsForValue().get(redisKey);
            if (concurrentValue != null) {
                return resolveExistingRecord(concurrentValue, fingerprint, replayLoader);
            }
            throw new ConflictException("Request is already in progress for this idempotency key.");
        }

        try {
            T result = action.get();
            Long resourceId = responseIdExtractor.apply(result);
            if (resourceId == null) {
                redisTemplate.delete(redisKey);
                throw new BadRequestException("Idempotent operation completed without a resource identifier.");
            }
            redisTemplate.opsForValue().set(redisKey, encodeDone(fingerprint, resourceId), DONE_TTL);
            return result;
        } catch (RuntimeException ex) {
            redisTemplate.delete(redisKey);
            throw ex;
        }
    }

    public String computeFingerprint(Object... parts) {
        StringBuilder raw = new StringBuilder();
        for (Object part : parts) {
            raw.append('|').append(serializePart(part));
        }
        return sha256Hex(raw.toString());
    }

    private <T> T resolveExistingRecord(String value, String fingerprint, Function<Long, T> replayLoader) {
        ParsedIdempotencyRecord record = parseRecord(value);

        if (!record.fingerprint().equals(fingerprint)) {
            throw new ConflictException("Idempotency key was already used with a different request payload.");
        }

        if (STATUS_PENDING.equals(record.status())) {
            throw new ConflictException("A request with this idempotency key is currently being processed.");
        }

        if (record.resourceId() == null) {
            throw new ConflictException("Idempotency record is incomplete. Please retry with a new idempotency key.");
        }

        return replayLoader.apply(record.resourceId());
    }

    private ParsedIdempotencyRecord parseRecord(String value) {
        String[] tokens = value.split("\\|", 3);
        if (tokens.length < 2) {
            throw new ConflictException("Invalid idempotency state detected.");
        }

        String status = tokens[0];
        String fingerprint = tokens[1];
        Long resourceId = null;

        if (STATUS_DONE.equals(status)) {
            if (tokens.length < 3) {
                throw new ConflictException("Invalid completed idempotency state detected.");
            }
            try {
                resourceId = Long.parseLong(tokens[2]);
            } catch (NumberFormatException ex) {
                throw new ConflictException("Invalid idempotency resource identifier detected.");
            }
        }

        return new ParsedIdempotencyRecord(status, fingerprint, resourceId);
    }

    private String encodePending(String fingerprint) {
        return STATUS_PENDING + "|" + fingerprint;
    }

    private String encodeDone(String fingerprint, Long resourceId) {
        return STATUS_DONE + "|" + fingerprint + "|" + resourceId;
    }

    private String buildRedisKey(String operation, String userScope, String key) {
        if (operation == null || operation.isBlank()) {
            throw new BadRequestException("Operation is required for idempotency.");
        }
        if (userScope == null || userScope.isBlank()) {
            throw new BadRequestException("User context is required for idempotency.");
        }
        return REDIS_PREFIX + ":" + operation.trim().toLowerCase() + ":" + userScope.trim().toLowerCase() + ":" + key;
    }

    private String normalizeIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BadRequestException("X-Idempotency-Key header is required.");
        }
        String normalized = idempotencyKey.trim();
        if (normalized.length() > 120) {
            throw new BadRequestException("X-Idempotency-Key must be 120 characters or fewer.");
        }
        return normalized;
    }

    private String serializePart(Object part) {
        if (part == null) {
            return "null";
        }
        if (part instanceof CharSequence || part instanceof Number || part instanceof Boolean || part instanceof Enum<?>) {
            return part.toString();
        }
        try {
            return objectMapper.writeValueAsString(part);
        } catch (JsonProcessingException ex) {
            throw new BadRequestException("Unable to build idempotency fingerprint.");
        }
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private record ParsedIdempotencyRecord(String status, String fingerprint, Long resourceId) {
    }
}