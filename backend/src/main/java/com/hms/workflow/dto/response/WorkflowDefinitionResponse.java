package com.hms.workflow.dto.response;

import com.hms.workflow.enums.WorkflowDefinitionStatus;
import com.hms.workflow.enums.WorkflowDomain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowDefinitionResponse {

    private Long id;
    private String definitionKey;
    private String name;
    private WorkflowDomain domain;
    private Integer versionNumber;
    private WorkflowDefinitionStatus status;
    private String initialStepCode;
    private String description;
    private List<WorkflowStepResponse> steps;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WorkflowStepResponse {
        private String stepCode;
        private String name;
        private Integer stepOrder;
        private String assigneeRole;
        private Integer slaMinutes;
        private Boolean terminalStep;
    }
}
