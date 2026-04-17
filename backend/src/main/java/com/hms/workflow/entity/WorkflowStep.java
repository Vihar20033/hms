package com.hms.workflow.entity;

import com.hms.common.entity.Auditable;
import com.hms.common.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
        name = "workflow_steps",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_workflow_step_code", columnNames = {"definition_id", "step_code"})
        },
        indexes = {
                @Index(name = "idx_workflow_step_def", columnList = "definition_id"),
                @Index(name = "idx_workflow_step_order", columnList = "step_order")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WorkflowStep extends Auditable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "definition_id", nullable = false)
    private WorkflowDefinition definition;

    @Column(name = "step_code", nullable = false, length = 80)
    private String stepCode;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignee_role", length = 40)
    private Role assigneeRole;

    @Column(name = "sla_minutes")
    private Integer slaMinutes;

    @Column(name = "terminal_step", nullable = false)
    private Boolean terminalStep;
}
