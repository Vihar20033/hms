package com.hms.workflow.repository;

import com.hms.workflow.entity.WorkflowInstance;
import com.hms.workflow.enums.WorkflowInstanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, Long> {

    List<WorkflowInstance> findByReferenceTypeAndReferenceId(String referenceType, String referenceId);

    List<WorkflowInstance> findByStatus(WorkflowInstanceStatus status);
}
