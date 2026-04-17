package com.hms.workflow.controller;

import com.hms.common.response.ApiResponse;
import com.hms.workflow.dto.request.CreateWorkflowDefinitionRequest;
import com.hms.workflow.dto.request.StartWorkflowInstanceRequest;
import com.hms.workflow.dto.request.TransitionWorkflowRequest;
import com.hms.workflow.dto.response.WorkflowDefinitionResponse;
import com.hms.workflow.dto.response.WorkflowInstanceResponse;
import com.hms.workflow.dto.response.WorkflowTaskResponse;
import com.hms.workflow.service.WorkflowEngineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class WorkflowEngineController {

    private final WorkflowEngineService workflowEngineService;

    @PostMapping("/definitions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WorkflowDefinitionResponse>> createDefinition(
            @Valid @RequestBody CreateWorkflowDefinitionRequest request) {
        WorkflowDefinitionResponse response = workflowEngineService.createDefinition(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Workflow definition created", HttpStatus.CREATED));
    }

    @PutMapping("/definitions/{definitionKey}/versions/{versionNumber}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WorkflowDefinitionResponse>> activateDefinition(
            @PathVariable String definitionKey,
            @PathVariable Integer versionNumber) {
        WorkflowDefinitionResponse response = workflowEngineService.activateDefinition(definitionKey, versionNumber);
        return ResponseEntity.ok(ApiResponse.success(response, "Workflow definition activated"));
    }

    @GetMapping("/definitions")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<WorkflowDefinitionResponse>>> listDefinitions(
            @RequestParam(required = false) String definitionKey) {
        return ResponseEntity.ok(ApiResponse.success(workflowEngineService.listDefinitions(definitionKey)));
    }

    @PostMapping("/instances")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    public ResponseEntity<ApiResponse<WorkflowInstanceResponse>> startInstance(
            @Valid @RequestBody StartWorkflowInstanceRequest request) {
        WorkflowInstanceResponse response = workflowEngineService.startInstance(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Workflow instance started", HttpStatus.CREATED));
    }

    @PostMapping("/instances/{instanceId}/transition")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    public ResponseEntity<ApiResponse<WorkflowInstanceResponse>> transition(
            @PathVariable Long instanceId,
            @Valid @RequestBody TransitionWorkflowRequest request) {
        WorkflowInstanceResponse response = workflowEngineService.transition(instanceId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Workflow transitioned successfully"));
    }

    @GetMapping("/instances/{instanceId}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    public ResponseEntity<ApiResponse<WorkflowInstanceResponse>> getInstance(@PathVariable Long instanceId) {
        return ResponseEntity.ok(ApiResponse.success(workflowEngineService.getInstance(instanceId)));
    }

    @GetMapping("/instances/{instanceId}/tasks")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<WorkflowTaskResponse>>> getTasks(@PathVariable Long instanceId) {
        return ResponseEntity.ok(ApiResponse.success(workflowEngineService.getTasks(instanceId)));
    }
}
