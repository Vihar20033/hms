package com.hms.common.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

import org.hibernate.envers.Audited;

@MappedSuperclass
@Getter
@Setter
@Audited
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@SoftDelete(columnName = "deleted", strategy = SoftDeleteType.DELETED)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Builder.Default
    private Long version = 0L;

    /**
     * Fix #7 - Concurrent Patient Soft-Delete
     * While Hibernate @SoftDelete filters records, we maintain an explicit field
     * to allow check if (patient.isDeleted()) at the start of transactions.
     */
    @Column(name = "deleted", insertable = false, updatable = false)
    private boolean deleted;

    @PrePersist
    protected void initializeVersion() {
        if (version == null) {
            version = 0L;
        }
    }

}
