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
public class StartWorkflowInstanceRequest {

    @NotBlank
    private String definitionKey;

    @NotBlank
    private String referenceType;

    @NotBlank
    private String referenceId;

    private String contextJson;
}
