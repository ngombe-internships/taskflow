package com.duva.taskflow.service;

import com.duva.taskflow.dto.ActivityLogResponseDTO;
import com.duva.taskflow.entity.*;
import com.duva.taskflow.repository.ActivityLogRepository;
import com.duva.taskflow.repository.TaskRepository;
import com.duva.taskflow.repository.UserRepository;
import com.duva.taskflow.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

/**
 * ActivityLogService - Logique métier de l'historique
 *
 * Responsabilités:
 * - Créer des logs d'activité
 * - Récupérer l'historique d'une tâche
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    // LOG CREATION (Internal - called by other services)

    @Transactional
    public void logTaskCreated(Task task, User user) {
        ActivityLog log = ActivityLog.builder()
                .task(task)
                .user(user)
                .actionType(ActivityLog.ActionType.TASK_CREATED)
                .description(user.getEmail() + " created task: " + task.getTitle())
                .build();

        activityLogRepository.save(log);
    }

    @Transactional
    public void logStatusChanged(Task task, User user, String oldStatus, String newStatus) {
        ActivityLog activityLog = ActivityLog.builder()
                .task(task)
                .user(user)
                .actionType(ActivityLog.ActionType.STATUS_CHANGED)
                .fieldChanged("status")
                .oldValue(oldStatus)
                .newValue(newStatus)
                .description(user.getEmail() + " changed status from " + oldStatus + " to " + newStatus)
                .build();

        activityLogRepository.save(activityLog);
        log.info("Status changed for task {}: {} -> {}", task.getId(), oldStatus, newStatus);
    }

    @Transactional
    public void logPriorityChanged(Task task, User user, String oldPriority, String newPriority) {
        ActivityLog log = ActivityLog.builder()
                .task(task)
                .user(user)
                .actionType(ActivityLog.ActionType.PRIORITY_CHANGED)
                .fieldChanged("priority")
                .oldValue(oldPriority)
                .newValue(newPriority)
                .description(user.getEmail() + " changed priority from " + oldPriority + " to " + newPriority)
                .build();

        activityLogRepository.save(log);
    }

    @Transactional
    public void logAssigned(Task task, User user, String assignedToEmail) {
        ActivityLog activityLog = ActivityLog.builder()
                .task(task)
                .user(user)
                .actionType(ActivityLog.ActionType.ASSIGNED)
                .newValue(assignedToEmail)
                .description(user.getEmail() + " assigned task to " + assignedToEmail)
                .build();

        activityLogRepository.save(activityLog);
        log.info("Task {} assigned to {}", task.getId(), assignedToEmail);
    }

    @Transactional
    public void logUnassigned(Task task, User user) {
        ActivityLog log = ActivityLog.builder()
                .task(task)
                .user(user)
                .actionType(ActivityLog.ActionType.UNASSIGNED)
                .description(user.getEmail() + " unassigned task")
                .build();

        activityLogRepository.save(log);
    }

    @Transactional
    public void logCommentAdded(Task task, User user) {
        ActivityLog log = ActivityLog.builder()
                .task(task)
                .user(user)
                .actionType(ActivityLog.ActionType.COMMENT_ADDED)
                .description(user.getEmail() + " added a comment")
                .build();

        activityLogRepository.save(log);
    }

    @Transactional
    public void logTaskUpdated(Task task, User user, String fieldName, String oldValue, String newValue) {
        ActivityLog log = ActivityLog.builder()
                .task(task)
                .user(user)
                .actionType(ActivityLog.ActionType.TASK_UPDATED)
                .fieldChanged(fieldName)
                .oldValue(oldValue)
                .newValue(newValue)
                .description(user.getEmail() + " changed " + fieldName)
                .build();

        activityLogRepository.save(log);
    }

    // READ

    public Page<ActivityLogResponseDTO> getTaskActivityLog(Long projectId, Long taskId, String email, Pageable pageable) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        //  Vérifier que la tâche appartient au projet
        if (!task.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Task does not belong to this project");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //  Vérifier que l'utilisateur est membre du projet
        projectMemberRepository.findByProjectAndUser(task.getProject(), user)
                .orElseThrow(() -> new RuntimeException("You are not a member of this project"));

        Page<ActivityLog> logs = activityLogRepository.findByTask(task, pageable);
        log.debug("Found {} activity logs for task: {}", logs.getTotalElements(), taskId);

        return logs.map(this::mapToDTO);
    }

    public Page<ActivityLogResponseDTO> getTaskActivityByType(Long projectId, Long taskId, String email,
                                                              ActivityLog.ActionType actionType, Pageable pageable) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Task does not belong to this project");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        projectMemberRepository.findByProjectAndUser(task.getProject(), user)
                .orElseThrow(() -> new RuntimeException("You are not a member of this project"));

        Page<ActivityLog> logs = activityLogRepository.findByTaskAndActionType(task, actionType, pageable);

        return logs.map(this::mapToDTO);
    }

    // HELPER

    private ActivityLogResponseDTO mapToDTO(ActivityLog log) {
        return ActivityLogResponseDTO.builder()
                .id(log.getId())
                .actionType(log.getActionType())
                .description(log.getDescription())
                .fieldChanged(log.getFieldChanged())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .user(log.getUser().getEmail())
                .createdAt(log.getCreatedAt())
                .build();
    }
}