package com.hms.workflow.exception;

public class WorkflowInstanceNotFoundException extends RuntimeException {

    public WorkflowInstanceNotFoundException(String message) {
        super(message);
    }
}
