package com.hms.workflow.repository;

import com.hms.workflow.entity.WorkflowDefinition;
import com.hms.workflow.enums.WorkflowDefinitionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, Long> {

    Optional<WorkflowDefinition> findTopByDefinitionKeyOrderByVersionNumberDesc(String definitionKey);

    Optional<WorkflowDefinition> findByDefinitionKeyAndVersionNumber(String definitionKey, Integer versionNumber);

    Optional<WorkflowDefinition> findByDefinitionKeyAndStatus(String definitionKey, WorkflowDefinitionStatus status);

    List<WorkflowDefinition> findByDefinitionKey(String definitionKey);
}
