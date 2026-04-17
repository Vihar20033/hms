package com.hms.workflow.repository;

import com.hms.workflow.entity.WorkflowTransition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransition, Long> {

    List<WorkflowTransition> findByDefinitionId(Long definitionId);

    List<WorkflowTransition> findByDefinitionIdAndFromStepCode(Long definitionId, String fromStepCode);
}
