package com.hms.workflow.dto.request;

import com.hms.common.enums.Role;
import com.hms.workflow.enums.WorkflowDomain;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateWorkflowDefinitionRequest {

    @NotBlank
    private String definitionKey;

    @NotBlank
    private String name;

    @NotNull
    private WorkflowDomain domain;

    @NotBlank
    private String initialStepCode;

    private String description;

    @Valid
    @NotEmpty
    private List<WorkflowStepRequest> steps;

    @Valid
    @NotEmpty
    private List<WorkflowTransitionRequest> transitions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WorkflowStepRequest {
        @NotBlank
        private String stepCode;

        @NotBlank
        private String name;

        @NotNull
        private Integer stepOrder;

        private Role assigneeRole;
        private Integer slaMinutes;

        @Builder.Default
        private Boolean terminalStep = false;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WorkflowTransitionRequest {
        @NotBlank
        private String fromStepCode;

        @NotBlank
        private String toStepCode;

        @NotBlank
        private String actionLabel;

        @Builder.Default
        private Boolean requiresApproval = false;

        private Role approvalRole;

        private String conditionExpression;
    }
}
