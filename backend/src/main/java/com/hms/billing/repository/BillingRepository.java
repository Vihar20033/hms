package com.hms.billing.repository;

import com.hms.billing.entity.Billing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BillingRepository extends JpaRepository<Billing, Long>, JpaSpecificationExecutor<Billing> {


    // SELECT * FROM billings WHERE patient_id = :patientId AND deleted = false
    List<Billing> findByPatientId(Long patientId);

    // SELECT SUM(net_amount) FROM billings WHERE payment_status = 'PAID' AND
    // deleted = false
    @Query("SELECT SUM(b.netAmount) FROM Billing b WHERE b.paymentStatus = com.hms.common.enums.PaymentStatus.PAID")
    BigDecimal sumTotalRevenue();

}
