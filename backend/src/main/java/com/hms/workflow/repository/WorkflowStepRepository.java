package com.hms.workflow.repository;

import com.hms.workflow.entity.WorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, Long> {

    List<WorkflowStep> findByDefinitionIdOrderByStepOrderAsc(Long definitionId);
}
