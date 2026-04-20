package com.hms.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityRevisionDTO<T> {
    private int revisionId;
    private Instant revisionDate;
    private String revisionType; // ADD, MOD, DEL
    private T entity;
}
