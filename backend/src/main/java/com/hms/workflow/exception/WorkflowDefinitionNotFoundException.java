package com.hms.workflow.exception;

public class WorkflowDefinitionNotFoundException extends RuntimeException {

    public WorkflowDefinitionNotFoundException(String message) {
        super(message);
    }
}
