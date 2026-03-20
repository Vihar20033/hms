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
    Optional<Billing> findByInvoiceNumber(String invoiceNumber);

    List<Billing> findByPatientId(UUID patientId);

    boolean existsByInvoiceNumber(String invoiceNumber);
}
