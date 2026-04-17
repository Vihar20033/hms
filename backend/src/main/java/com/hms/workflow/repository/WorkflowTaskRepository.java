package com.hms.workflow.repository;

import com.hms.workflow.entity.WorkflowTask;
import com.hms.workflow.enums.WorkflowTaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkflowTaskRepository extends JpaRepository<WorkflowTask, Long> {

    List<WorkflowTask> findByInstanceIdOrderByCreatedAtAsc(Long instanceId);

    List<WorkflowTask> findByAssigneeUserIdAndStatus(Long assigneeUserId, WorkflowTaskStatus status);

    List<WorkflowTask> findByInstanceIdAndStatus(Long instanceId, WorkflowTaskStatus status);
}
