package com.hms.workflow.entity;

import com.hms.common.entity.Auditable;
import com.hms.workflow.enums.WorkflowDefinitionStatus;
import com.hms.workflow.enums.WorkflowDomain;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;

@Entity
@Audited
@Table(
        name = "workflow_definitions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_workflow_key_version", columnNames = {"definition_key", "version_number"})
        },
        indexes = {
                @Index(name = "idx_workflow_definition_key", columnList = "definition_key"),
                @Index(name = "idx_workflow_definition_domain", columnList = "domain"),
                @Index(name = "idx_workflow_definition_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WorkflowDefinition extends Auditable {

    @Column(name = "definition_key", nullable = false, length = 100)
    private String definitionKey;

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WorkflowDomain domain;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkflowDefinitionStatus status;

    @Column(name = "initial_step_code", nullable = false, length = 80)
    private String initialStepCode;

    @Column(columnDefinition = "TEXT")
    private String description;
}
