package com.hms.billing.repository;

import com.hms.billing.entity.Billing;
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
