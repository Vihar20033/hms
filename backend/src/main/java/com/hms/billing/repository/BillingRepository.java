package com.hms.billing.repository;

import com.hms.billing.entity.Billing;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BillingRepository extends JpaRepository<Billing, Long> {

    List<Billing> findByPatientId(Long patientId);

    Slice<Billing> findByPatientId(Long patientId, Pageable pageable);

    @Query("""
            SELECT b FROM Billing b
            JOIN b.patient p
            WHERE LOWER(b.invoiceNumber) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(p.contactNumber) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(COALESCE(p.email, '')) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(COALESCE(b.insuranceProvider, '')) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(COALESCE(b.insuranceClaimNumber, '')) LIKE LOWER(CONCAT('%', :query, '%'))
               OR FUNCTION('soundex', LOWER(b.invoiceNumber)) = FUNCTION('soundex', LOWER(:query))
               OR FUNCTION('soundex', LOWER(p.name)) = FUNCTION('soundex', LOWER(:query))
               OR FUNCTION('soundex', LOWER(p.contactNumber)) = FUNCTION('soundex', LOWER(:query))
               OR FUNCTION('soundex', LOWER(COALESCE(p.email, ''))) = FUNCTION('soundex', LOWER(:query))
               OR FUNCTION('soundex', LOWER(COALESCE(b.insuranceProvider, ''))) = FUNCTION('soundex', LOWER(:query))
               OR FUNCTION('soundex', LOWER(COALESCE(b.insuranceClaimNumber, ''))) = FUNCTION('soundex', LOWER(:query))
            """)
    Slice<Billing> searchBillings(@Param("query") String query, Pageable pageable);

    boolean existsByAppointmentId(Long appointmentId);

    @Query("SELECT SUM(b.netAmount) FROM Billing b WHERE " +
            "b.paymentStatus = com.hms.common.enums.PaymentStatus.PAID")
    BigDecimal sumTotalRevenue();

    @Query(value = "SELECT fn_calculate_net_amount(:base, :tax, :discount)", nativeQuery = true)
    BigDecimal calculateNetWithFunction(BigDecimal base, BigDecimal tax, BigDecimal discount);

    @Modifying
    @Transactional
    @Query(value = "UPDATE billings SET deleted = false WHERE id = :id", nativeQuery = true)
    int restore(@Param("id") Long id);
}
