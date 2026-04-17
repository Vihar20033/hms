package com.hms.workflow.entity;

import com.hms.common.entity.Auditable;
import com.hms.common.enums.Role;
import com.hms.workflow.enums.WorkflowTaskStatus;
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

import java.time.Instant;

@Entity
@Audited
@Table(
        name = "workflow_tasks",
        indexes = {
                @Index(name = "idx_workflow_task_instance", columnList = "instance_id"),
                @Index(name = "idx_workflow_task_assignee", columnList = "assignee_user_id,status"),
                @Index(name = "idx_workflow_task_step", columnList = "step_code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WorkflowTask extends Auditable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instance_id", nullable = false)
    private WorkflowInstance instance;

    @Column(name = "step_code", nullable = false, length = 80)
    private String stepCode;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(name = "assignee_user_id")
    private Long assigneeUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignee_role", length = 40)
    private Role assigneeRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkflowTaskStatus status;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "due_at")
    private Instant dueAt;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
