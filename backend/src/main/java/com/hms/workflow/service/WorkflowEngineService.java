package com.hms.workflow.service;

import com.hms.workflow.dto.request.CreateWorkflowDefinitionRequest;
import com.hms.workflow.dto.request.StartWorkflowInstanceRequest;
import com.hms.workflow.dto.request.TransitionWorkflowRequest;
import com.hms.workflow.dto.response.WorkflowDefinitionResponse;
import com.hms.workflow.dto.response.WorkflowInstanceResponse;
import com.hms.workflow.dto.response.WorkflowTaskResponse;

import java.util.List;

public interface WorkflowEngineService {

    WorkflowDefinitionResponse createDefinition(CreateWorkflowDefinitionRequest request);

    WorkflowDefinitionResponse activateDefinition(String definitionKey, Integer versionNumber);

    List<WorkflowDefinitionResponse> listDefinitions(String definitionKey);

    WorkflowInstanceResponse startInstance(StartWorkflowInstanceRequest request);

    WorkflowInstanceResponse transition(Long instanceId, TransitionWorkflowRequest request);

    WorkflowInstanceResponse getInstance(Long instanceId);

    List<WorkflowTaskResponse> getTasks(Long instanceId);
}
