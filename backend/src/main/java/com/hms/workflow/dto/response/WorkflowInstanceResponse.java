package com.hms.workflow.dto.response;

import com.hms.workflow.enums.WorkflowDomain;
import com.hms.workflow.enums.WorkflowInstanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowInstanceResponse {

    private Long id;
    private String definitionKey;
    private Integer definitionVersion;
    private WorkflowDomain domain;
    private String referenceType;
    private String referenceId;
    private String currentStepCode;
    private WorkflowInstanceStatus status;
    private Instant startedAt;
    private Instant completedAt;
    private List<WorkflowTaskResponse> tasks;
}
