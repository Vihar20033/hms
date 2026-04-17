package com.hms.workflow.entity;

import com.hms.common.entity.Auditable;
import com.hms.workflow.enums.WorkflowDomain;
import com.hms.workflow.enums.WorkflowInstanceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;

import java.time.Instant;

@Entity
@Audited
@Table(
        name = "workflow_instances",
        indexes = {
                @Index(name = "idx_workflow_instance_def", columnList = "definition_key,definition_version"),
                @Index(name = "idx_workflow_instance_ref", columnList = "reference_type,reference_id"),
                @Index(name = "idx_workflow_instance_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WorkflowInstance extends Auditable {

    @Column(name = "definition_key", nullable = false, length = 100)
    private String definitionKey;

    @Column(name = "definition_version", nullable = false)
    private Integer definitionVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WorkflowDomain domain;

    @Column(name = "reference_type", nullable = false, length = 80)
    private String referenceType;

    @Column(name = "reference_id", nullable = false, length = 80)
    private String referenceId;

    @Column(name = "current_step_code", nullable = false, length = 80)
    private String currentStepCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkflowInstanceStatus status;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "context_json", columnDefinition = "TEXT")
    private String contextJson;
}
