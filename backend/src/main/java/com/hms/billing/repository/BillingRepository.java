package com.hms.billing.repository;

import com.hms.billing.entity.Billing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    @org.springframework.data.jpa.repository.Query("SELECT SUM(b.netAmount) FROM Billing b WHERE b.paymentStatus = com.hms.common.enums.PaymentStatus.PAID")
    java.math.BigDecimal sumTotalRevenue();

    // SELECT SUM(net_amount) FROM billings WHERE payment_status = 'PAID' AND created_at >= :start AND created_at <= :end AND deleted = false
    @org.springframework.data.jpa.repository.Query("SELECT SUM(b.netAmount) FROM Billing b WHERE b.paymentStatus = com.hms.common.enums.PaymentStatus.PAID AND b.createdAt >= :start AND b.createdAt <= :end")
    java.math.BigDecimal sumTodayRevenue(@org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start, @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);
}
