package com.hms.workflow.dto.response;

import com.hms.workflow.enums.WorkflowTaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowTaskResponse {

    private Long id;
    private String stepCode;
    private String title;
    private Long assigneeUserId;
    private String assigneeRole;
    private WorkflowTaskStatus status;
    private Instant startedAt;
    private Instant completedAt;
    private Instant dueAt;
    private String notes;
}
