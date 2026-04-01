package com.hms.billing.repository;

import com.hms.billing.entity.Billing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BillingRepository extends JpaRepository<Billing, Long> {

    List<Billing> findByPatientId(Long patientId);

    @Query("SELECT SUM(b.netAmount) FROM Billing b WHERE " +
            "b.paymentStatus = com.hms.common.enums.PaymentStatus.PAID")
    BigDecimal sumTotalRevenue();
}
