package com.hms.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    @org.hibernate.annotations.ColumnDefault("false")
    @lombok.Builder.Default
    private boolean deleted = false;

    @Version
    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    @lombok.Builder.Default
    private Long version = 0L;
}
