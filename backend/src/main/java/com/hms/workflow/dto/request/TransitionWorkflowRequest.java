package com.hms.workflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransitionWorkflowRequest {

    @NotBlank
    private String actionLabel;

    @Builder.Default
    private Boolean approvalGranted = false;

    private String notes;

    private Long assigneeUserId;
}
