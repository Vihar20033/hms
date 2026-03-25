package com.hms.billing.repository;

import com.hms.billing.entity.Billing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

@Repository
public interface BillingRepository extends JpaRepository<Billing, UUID>, JpaSpecificationExecutor<Billing> {

    // SELECT * FROM billings WHERE invoice_number = :invoiceNumber AND deleted = false
    Optional<Billing> findByInvoiceNumber(String invoiceNumber);

    // SELECT * FROM billings WHERE patient_id = :patientId AND deleted = false
    List<Billing> findByPatientId(UUID patientId);

    // SELECT * FROM billings WHERE patient_email = :email AND deleted = false
    List<Billing> findByPatientEmail(String email);

    // SELECT EXISTS(SELECT 1 FROM billings WHERE invoice_number = :invoiceNumber AND deleted = false)
    boolean existsByInvoiceNumber(String invoiceNumber);

    // SELECT SUM(net_amount) FROM billings WHERE payment_status = 'PAID' AND deleted = false
    @Query("SELECT SUM(b.netAmount) FROM Billing b WHERE b.paymentStatus = com.hms.common.enums.PaymentStatus.PAID")
    BigDecimal sumTotalRevenue();

    // SELECT SUM(net_amount) FROM billings WHERE payment_status = 'PAID' AND created_at >= :start AND created_at <= :end AND deleted = false
    @Query("SELECT SUM(b.netAmount) FROM Billing b WHERE b.paymentStatus = com.hms.common.enums.PaymentStatus.PAID AND b.createdAt >= :start AND b.createdAt <= :end")
    BigDecimal sumTodayRevenue(@Param("start") LocalDateTime start, @Param("end") java.time.LocalDateTime end);
}
