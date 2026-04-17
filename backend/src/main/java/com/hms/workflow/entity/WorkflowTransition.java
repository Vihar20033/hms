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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;

@Entity
@Audited
@Table(
        name = "workflow_transitions",
        indexes = {
                @Index(name = "idx_workflow_transition_def", columnList = "definition_id"),
                @Index(name = "idx_workflow_transition_from", columnList = "from_step_code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WorkflowTransition extends Auditable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "definition_id", nullable = false)
    private WorkflowDefinition definition;

    @Column(name = "from_step_code", nullable = false, length = 80)
    private String fromStepCode;

    @Column(name = "to_step_code", nullable = false, length = 80)
    private String toStepCode;

    @Column(name = "action_label", nullable = false, length = 100)
    private String actionLabel;

    @Column(name = "requires_approval", nullable = false)
    private Boolean requiresApproval;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_role", length = 40)
    private Role approvalRole;

    @Column(name = "condition_expression", length = 500)
    private String conditionExpression;
}
