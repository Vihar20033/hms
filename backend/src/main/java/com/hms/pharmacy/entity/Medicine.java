package com.hms.pharmacy.entity;

import com.hms.common.entity.Auditable;
import com.hms.common.enums.MedicineCategory;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Table(
        name = "medicines",
        indexes = {
                @Index(name = "idx_medicine_code", columnList = "medicineCode"),
                @Index(name = "idx_medicine_category", columnList = "category"),
                @Index(name = "idx_medicine_active", columnList = "is_active")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Medicine extends Auditable {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String medicineCode;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private MedicineCategory category;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    /**
     * Fix #5 – Pharmacy Stock Integer Overflow:
     * Switched from Integer (max ~2.1B) to Long to safely handle bulk orders
     * and cumulative transaction volumes in high-throughput pharmacy systems.
     */
    @Column(name = "quantity_in_stock", nullable = false)
    private Long quantityInStock;

    /**
     * Fix #5 – Max Transaction Limit guard:
     * No single restock/dispense transaction may exceed this threshold,
     * preventing accidental bulk data-entry mistakes.
     */
    public static final long MAX_TRANSACTION_LIMIT = 100_000L;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "reorder_level")
    private Integer reorderLevel;

    /**
     * Fix #8 – Prescribed Dosage Range Validation:
     * Stores the maximum safe dispensable quantity per prescription
     * for this medicine. The service layer checks quantity <= maxSafeDose
     * and triggers a warning when exceeded.
     */
    @Column(name = "max_safe_dose")
    private Integer maxSafeDose;

    @Column(name = "dosage")
    private String dosage;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

}
