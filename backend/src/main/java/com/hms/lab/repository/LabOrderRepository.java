package com.hms.lab.repository;

import com.hms.common.enums.LabOrderStatus;
import com.hms.lab.entity.LabOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LabOrderRepository extends JpaRepository<LabOrder, Long> {
    List<LabOrder> findByStatusOrderByCreatedAtAsc(LabOrderStatus status);
    List<LabOrder> findByPatientIdOrderByCreatedAtDesc(Long patientId);
}
