package com.hms.pharmacy.entity;

import com.hms.common.entity.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "inventory_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransaction extends Auditable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(nullable = false)
    private String transactionType; // IN or OUT

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "reference_id")
    private UUID referenceId; // e.g. Prescription ID
    
    @Column(columnDefinition = "TEXT")
    private String notes;
}
