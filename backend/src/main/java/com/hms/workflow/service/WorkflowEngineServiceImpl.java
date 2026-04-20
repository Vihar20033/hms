package com.hms.workflow.service;

import com.hms.common.enums.Role;
import com.hms.workflow.dto.request.CreateWorkflowDefinitionRequest;
import com.hms.workflow.dto.request.StartWorkflowInstanceRequest;
import com.hms.workflow.dto.request.TransitionWorkflowRequest;
import com.hms.workflow.dto.response.WorkflowDefinitionResponse;
import com.hms.workflow.dto.response.WorkflowInstanceResponse;
import com.hms.workflow.dto.response.WorkflowTaskResponse;
import com.hms.workflow.entity.WorkflowDefinition;
import com.hms.workflow.entity.WorkflowInstance;
import com.hms.workflow.entity.WorkflowStep;
import com.hms.workflow.entity.WorkflowTask;
import com.hms.workflow.entity.WorkflowTransition;
import com.hms.workflow.enums.WorkflowDefinitionStatus;
import com.hms.workflow.enums.WorkflowInstanceStatus;
import com.hms.workflow.enums.WorkflowTaskStatus;
import com.hms.workflow.exception.WorkflowDefinitionNotFoundException;
import com.hms.workflow.exception.WorkflowInstanceNotFoundException;
import com.hms.workflow.exception.WorkflowStateException;
import com.hms.workflow.exception.WorkflowValidationException;
import com.hms.workflow.repository.WorkflowDefinitionRepository;
import com.hms.workflow.repository.WorkflowInstanceRepository;
import com.hms.workflow.repository.WorkflowStepRepository;
import com.hms.workflow.repository.WorkflowTaskRepository;
import com.hms.workflow.repository.WorkflowTransitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkflowEngineServiceImpl implements WorkflowEngineService {

    private final WorkflowDefinitionRepository definitionRepository;
    private final WorkflowStepRepository stepRepository;
    private final WorkflowTransitionRepository transitionRepository;
    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowTaskRepository taskRepository;

    @Override
    public WorkflowDefinitionResponse createDefinition(CreateWorkflowDefinitionRequest request) {
        validateDefinitionRequest(request);

        int nextVersion = definitionRepository.findTopByDefinitionKeyOrderByVersionNumberDesc(request.getDefinitionKey())
                .map(existing -> existing.getVersionNumber() + 1)
                .orElse(1);

        WorkflowDefinition definition = WorkflowDefinition.builder()
                .definitionKey(request.getDefinitionKey())
                .name(request.getName())
                .domain(request.getDomain())
                .versionNumber(nextVersion)
                .status(WorkflowDefinitionStatus.DRAFT)
                .initialStepCode(request.getInitialStepCode())
                .description(request.getDescription())
                .build();
        definition = definitionRepository.save(definition);

        WorkflowDefinition savedDefinition = definition;
        List<WorkflowStep> steps = request.getSteps().stream()
            .<WorkflowStep>map(step -> WorkflowStep.builder()
                .definition(savedDefinition)
                .stepCode(step.getStepCode())
                .name(step.getName())
                .stepOrder(step.getStepOrder())
                .assigneeRole(step.getAssigneeRole())
                .slaMinutes(step.getSlaMinutes())
                .terminalStep(Boolean.TRUE.equals(step.getTerminalStep()))
                .build())
            .collect(Collectors.toList());
        stepRepository.saveAll(steps);

        List<WorkflowTransition> transitions = request.getTransitions().stream()
            .<WorkflowTransition>map(transition -> WorkflowTransition.builder()
                        .definition(savedDefinition)
                        .fromStepCode(transition.getFromStepCode())
                        .toStepCode(transition.getToStepCode())
                        .actionLabel(transition.getActionLabel())
                        .requiresApproval(Boolean.TRUE.equals(transition.getRequiresApproval()))
                        .approvalRole(transition.getApprovalRole())
                        .conditionExpression(transition.getConditionExpression())
                        .build())
            .collect(Collectors.toList());
        transitionRepository.saveAll(transitions);

        return mapDefinition(savedDefinition, steps);
    }

    @Override
    public WorkflowDefinitionResponse activateDefinition(String definitionKey, Integer versionNumber) {
        WorkflowDefinition target = definitionRepository
                .findByDefinitionKeyAndVersionNumber(definitionKey, versionNumber)
                .orElseThrow(() -> new WorkflowDefinitionNotFoundException("Workflow definition version not found"));

        List<WorkflowStep> steps = stepRepository.findByDefinitionIdOrderByStepOrderAsc(target.getId());
        if (steps.isEmpty()) {
            throw new WorkflowStateException("Cannot activate workflow without steps");
        }

        definitionRepository.findByDefinitionKeyAndStatus(definitionKey, WorkflowDefinitionStatus.ACTIVE)
                .ifPresent(active -> {
                    active.setStatus(WorkflowDefinitionStatus.INACTIVE);
                    definitionRepository.save(active);
                });

        target.setStatus(WorkflowDefinitionStatus.ACTIVE);
        WorkflowDefinition saved = definitionRepository.save(target);
        return mapDefinition(saved, steps);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowDefinitionResponse> listDefinitions(String definitionKey) {
        List<WorkflowDefinition> definitions = StringUtils.hasText(definitionKey)
                ? definitionRepository.findByDefinitionKey(definitionKey)
                : definitionRepository.findAll();

        return definitions.stream()
                .sorted(Comparator.comparing(WorkflowDefinition::getDefinitionKey)
                        .thenComparing(WorkflowDefinition::getVersionNumber).reversed())
                .map(def -> mapDefinition(def, stepRepository.findByDefinitionIdOrderByStepOrderAsc(def.getId())))
                .toList();
    }

    @Override
    public WorkflowInstanceResponse startInstance(StartWorkflowInstanceRequest request) {
        WorkflowDefinition definition = definitionRepository
                .findByDefinitionKeyAndStatus(request.getDefinitionKey(), WorkflowDefinitionStatus.ACTIVE)
                .orElseThrow(() -> new WorkflowDefinitionNotFoundException("Active workflow definition not found for key"));

        Map<String, WorkflowStep> stepMap = stepRepository.findByDefinitionIdOrderByStepOrderAsc(definition.getId()).stream()
                .collect(Collectors.toMap(WorkflowStep::getStepCode, Function.identity()));

        WorkflowStep initialStep = stepMap.get(definition.getInitialStepCode());
        if (initialStep == null) {
            throw new WorkflowStateException("Initial step missing in workflow definition");
        }

        WorkflowInstance instance = WorkflowInstance.builder()
                .definitionKey(definition.getDefinitionKey())
                .definitionVersion(definition.getVersionNumber())
                .domain(definition.getDomain())
                .referenceType(request.getReferenceType())
                .referenceId(request.getReferenceId())
                .currentStepCode(definition.getInitialStepCode())
                .status(WorkflowInstanceStatus.RUNNING)
                .startedAt(Instant.now())
                .contextJson(request.getContextJson())
                .build();
        instance = instanceRepository.save(instance);

        WorkflowTask firstTask = createTaskForStep(instance, initialStep, null);
        return mapInstance(instance, List.of(firstTask));
    }

    @Override
    public WorkflowInstanceResponse transition(Long instanceId, TransitionWorkflowRequest request) {
        WorkflowInstance instance = instanceRepository.findById(instanceId)
                .orElseThrow(() -> new WorkflowInstanceNotFoundException("Workflow instance not found"));

        if (instance.getStatus() != WorkflowInstanceStatus.RUNNING) {
            throw new WorkflowStateException("Only RUNNING instances can transition");
        }

        WorkflowDefinition definition = definitionRepository
                .findByDefinitionKeyAndVersionNumber(instance.getDefinitionKey(), instance.getDefinitionVersion())
                .orElseThrow(() -> new WorkflowDefinitionNotFoundException("Workflow definition for instance not found"));

        List<WorkflowStep> steps = stepRepository.findByDefinitionIdOrderByStepOrderAsc(definition.getId());
        Map<String, WorkflowStep> stepMap = steps.stream()
                .collect(Collectors.toMap(WorkflowStep::getStepCode, Function.identity()));

        WorkflowStep currentStep = stepMap.get(instance.getCurrentStepCode());
        if (currentStep == null) {
            throw new WorkflowStateException("Current step does not exist in workflow definition");
        }

        List<WorkflowTransition> candidates = transitionRepository
                .findByDefinitionIdAndFromStepCode(definition.getId(), currentStep.getStepCode());

        WorkflowTransition selectedTransition = candidates.stream()
                .filter(t -> t.getActionLabel().equalsIgnoreCase(request.getActionLabel()))
                .findFirst()
                .orElseThrow(() -> new WorkflowValidationException("No valid transition for requested action"));

        enforceApprovalIfRequired(selectedTransition, request);

        WorkflowStep targetStep = stepMap.get(selectedTransition.getToStepCode());
        if (targetStep == null) {
            throw new WorkflowStateException("Target step not present in workflow definition");
        }

        List<WorkflowTask> openTasks = taskRepository.findByInstanceIdAndStatus(instance.getId(), WorkflowTaskStatus.PENDING);
        openTasks.addAll(taskRepository.findByInstanceIdAndStatus(instance.getId(), WorkflowTaskStatus.IN_PROGRESS));
        openTasks.forEach(task -> {
            task.setStatus(WorkflowTaskStatus.COMPLETED);
            task.setCompletedAt(Instant.now());
            if (StringUtils.hasText(request.getNotes())) {
                task.setNotes(request.getNotes());
            }
        });
        taskRepository.saveAll(openTasks);

        instance.setCurrentStepCode(targetStep.getStepCode());
        if (Boolean.TRUE.equals(targetStep.getTerminalStep())) {
            instance.setStatus(WorkflowInstanceStatus.COMPLETED);
            instance.setCompletedAt(Instant.now());
            instanceRepository.save(instance);
            return mapInstance(instance, taskRepository.findByInstanceIdOrderByCreatedAtAsc(instance.getId()));
        }

        WorkflowTask nextTask = createTaskForStep(instance, targetStep, request.getAssigneeUserId());
        instanceRepository.save(instance);

        List<WorkflowTask> tasks = taskRepository.findByInstanceIdOrderByCreatedAtAsc(instance.getId());
        if (tasks.stream().noneMatch(t -> t.getId().equals(nextTask.getId()))) {
            tasks.add(nextTask);
        }
        return mapInstance(instance, tasks);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowInstanceResponse getInstance(Long instanceId) {
        WorkflowInstance instance = instanceRepository.findById(instanceId)
                .orElseThrow(() -> new WorkflowInstanceNotFoundException("Workflow instance not found"));
        List<WorkflowTask> tasks = taskRepository.findByInstanceIdOrderByCreatedAtAsc(instanceId);
        return mapInstance(instance, tasks);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowTaskResponse> getTasks(Long instanceId) {
        return taskRepository.findByInstanceIdOrderByCreatedAtAsc(instanceId).stream()
                .map(this::mapTask)
                .toList();
    }

    private void validateDefinitionRequest(CreateWorkflowDefinitionRequest request) {
        Set<String> stepCodes = request.getSteps().stream()
                .map(CreateWorkflowDefinitionRequest.WorkflowStepRequest::getStepCode)
                .collect(Collectors.toSet());

        if (!stepCodes.contains(request.getInitialStepCode())) {
            throw new WorkflowValidationException("Initial step code must exist in steps list");
        }

        request.getTransitions().forEach(transition -> {
            if (!stepCodes.contains(transition.getFromStepCode()) || !stepCodes.contains(transition.getToStepCode())) {
                throw new WorkflowValidationException("Transition references undefined step code");
            }
        });
    }

    private void enforceApprovalIfRequired(WorkflowTransition transition, TransitionWorkflowRequest request) {
        if (!Boolean.TRUE.equals(transition.getRequiresApproval())) {
            return;
        }

        if (!Boolean.TRUE.equals(request.getApprovalGranted())) {
            throw new WorkflowStateException("Approval is required for this transition");
        }

        Role requiredRole = transition.getApprovalRole();
        if (requiredRole == null) {
            return;
        }

        String authority = "ROLE_" + requiredRole.name();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean hasAuthority = authentication != null
            && authentication.getAuthorities() != null
            && authentication.getAuthorities().stream().anyMatch(auth -> authority.equals(auth.getAuthority()));

        if (!hasAuthority) {
            throw new SecurityException("Current user is not authorized to approve this transition");
        }
    }

    private WorkflowTask createTaskForStep(WorkflowInstance instance, WorkflowStep step, Long explicitAssigneeUserId) {
        Instant now = Instant.now();
        Instant dueAt = step.getSlaMinutes() == null ? null : now.plus(step.getSlaMinutes(), ChronoUnit.MINUTES);

        WorkflowTask task = WorkflowTask.builder()
                .instance(instance)
                .stepCode(step.getStepCode())
                .title(step.getName())
                .assigneeUserId(explicitAssigneeUserId)
                .assigneeRole(step.getAssigneeRole())
                .status(WorkflowTaskStatus.PENDING)
                .dueAt(dueAt)
                .build();

        return taskRepository.save(task);
    }

    private WorkflowDefinitionResponse mapDefinition(WorkflowDefinition definition, List<WorkflowStep> steps) {
        return WorkflowDefinitionResponse.builder()
                .id(definition.getId())
                .definitionKey(definition.getDefinitionKey())
                .name(definition.getName())
                .domain(definition.getDomain())
                .versionNumber(definition.getVersionNumber())
                .status(definition.getStatus())
                .initialStepCode(definition.getInitialStepCode())
                .description(definition.getDescription())
                .steps(steps.stream().map(step -> WorkflowDefinitionResponse.WorkflowStepResponse.builder()
                        .stepCode(step.getStepCode())
                        .name(step.getName())
                        .stepOrder(step.getStepOrder())
                        .assigneeRole(step.getAssigneeRole() == null ? null : step.getAssigneeRole().name())
                        .slaMinutes(step.getSlaMinutes())
                        .terminalStep(step.getTerminalStep())
                        .build()).toList())
                .build();
    }

    private WorkflowInstanceResponse mapInstance(WorkflowInstance instance, List<WorkflowTask> tasks) {
        return WorkflowInstanceResponse.builder()
                .id(instance.getId())
                .definitionKey(instance.getDefinitionKey())
                .definitionVersion(instance.getDefinitionVersion())
                .domain(instance.getDomain())
                .referenceType(instance.getReferenceType())
                .referenceId(instance.getReferenceId())
                .currentStepCode(instance.getCurrentStepCode())
                .status(instance.getStatus())
                .startedAt(instance.getStartedAt())
                .completedAt(instance.getCompletedAt())
                .tasks(tasks.stream().map(this::mapTask).toList())
                .build();
    }

    private WorkflowTaskResponse mapTask(WorkflowTask task) {
        return WorkflowTaskResponse.builder()
                .id(task.getId())
                .stepCode(task.getStepCode())
                .title(task.getTitle())
                .assigneeUserId(task.getAssigneeUserId())
                .assigneeRole(task.getAssigneeRole() == null ? null : task.getAssigneeRole().name())
                .status(task.getStatus())
                .startedAt(task.getStartedAt())
                .completedAt(task.getCompletedAt())
                .dueAt(task.getDueAt())
                .notes(task.getNotes())
                .build();
    }
}
