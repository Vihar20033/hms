package com.hms.common.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Custom deserializer for LocalDateTime that accepts multiple date/datetime formats:
 * - "2026-03-20"                        (date only)
 * - "2026-03-20T14:30:00"              (no millis, no timezone)
 * - "2026-03-20T14:30:00.123"          (with millis)
 * - "2026-03-20T10:30:00Z"            (UTC/Z suffix)
 * - "2026-03-20T10:30:00.123Z"        (UTC/Z with millis – sent by Angular)
 * - "2026-03-20T10:30:00+05:30"       (offset timezone)
 */
public class LocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter DATETIME_MILLIS_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public LocalDateTimeDeserializer() {
        super(LocalDateTime.class);
    }

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext text)
            throws IOException {

        String dateString = p.getValueAsString();

        if (dateString == null || dateString.isEmpty()) {
            return null;
        }

        try {
            // Handle ISO 8601 with timezone (Z or offset like +05:30)
            if (dateString.endsWith("Z")) {
                // "2026-03-18T10:41:45.622Z" -> parse as instant with UTC
                try {
                    return OffsetDateTime.parse(dateString.replace("Z", "+00:00"), 
                            DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                            .toLocalDateTime();
                } catch (Exception ignored) {
                    // fall through
                }
            } else if (dateString.contains("T") && (dateString.contains("+") || 
                       (dateString.lastIndexOf("-") > dateString.indexOf("T")))) {
                // Has timezone offset (e.g., "2026-03-18T10:41:45+05:30")
                try {
                    return OffsetDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                            .toLocalDateTime();
                } catch (Exception ignored) {
                    // fall through
                }
            }

            // Handle plain datetime and date formats
            if (dateString.contains("T")) {
                // Try with milliseconds first
                if (dateString.contains(".")) {
                    try {
                        return LocalDateTime.parse(dateString, DATETIME_MILLIS_FORMATTER);
                    } catch (Exception ignored) {
                        // fall through
                    }
                }
                // Try without milliseconds
                try {
                    return LocalDateTime.parse(dateString, DATETIME_FORMATTER);
                } catch (Exception ignored) {
                    // fall through
                }
            } 
            
            // Date only -> midnight
            LocalDate date = LocalDate.parse(dateString, DATE_FORMATTER);
            return date.atTime(LocalTime.MIDNIGHT);
        } catch (Exception e) {
            throw text.weirdStringException(dateString, LocalDateTime.class,
                "Invalid date/datetime format. Accepted: 'yyyy-MM-dd', 'yyyy-MM-ddTHH:mm:ss', 'yyyy-MM-ddTHH:mm:ss.SSS', or ISO-8601 with timezone.");
        }
    }
}
