package com.duva.taskflow.service;

import com.duva.taskflow.dto.SubTaskRequestDTO;
import com.duva.taskflow.dto.SubTaskResponseDTO;
import com.duva.taskflow.entity.*;
import com.duva.taskflow.entity.enums.Status;
import com.duva.taskflow.repository.SubTaskRepository;
import com.duva.taskflow.repository.TaskRepository;
import com.duva.taskflow.repository.UserRepository;
import com.duva.taskflow.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SubTaskService - Logique métier des sous-tâches
 *
 * Responsabilités:
 * - Créer/lire/mettre à jour/supprimer les sous-tâches
 * - Vérifier les permissions du projet
 * - Auto-update du statut de la tâche parente
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubTaskService {

    private final SubTaskRepository subTaskRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    // HELPER - GET CURRENT USER

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // HELPER - VERIFY PERMISSIONS

    private Task verifyTaskOwnership(Long taskId) {
        User currentUser = getCurrentUser();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        //  Vérifier que l'utilisateur est MEMBER ou ADMIN du projet
        ProjectMember member = projectMemberRepository.findByProjectAndUser(task.getProject(), currentUser)
                .orElseThrow(() -> new RuntimeException("You are not a member of this project"));

        if (member.getRole() == ProjectMember.ProjectRole.VIEWER) {
            throw new RuntimeException("Viewers can only read, not modify");
        }

        return task;
    }

    // HELPER - MAPPING

    private SubTaskResponseDTO mapToDTO(SubTask subTask) {
        return SubTaskResponseDTO.builder()
                .id(subTask.getId())
                .title(subTask.getTitle())
                .description(subTask.getDescription())
                .status(subTask.getStatus())
                .createdAt(subTask.getCreatedAt())
                .updatedAt(subTask.getUpdatedAt())
                .build();
    }

    // CREATE

    public SubTaskResponseDTO createSubTask(Long taskId, SubTaskRequestDTO dto) {
        Task task = verifyTaskOwnership(taskId);

        SubTask subTask = SubTask.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(Status.PENDING)
                .task(task)
                .build();

        SubTask saved = subTaskRepository.save(subTask);
        log.info("SubTask created: {} for task: {}", saved.getId(), taskId);

        //  Auto-update parent task status
        updateParentTaskStatus(taskId);

        return mapToDTO(saved);
    }

    // READ

    public Page<SubTaskResponseDTO> getSubTasks(Long taskId, Pageable pageable) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        User currentUser = getCurrentUser();

        //  Vérifier que l'utilisateur est membre du projet (même VIEWER peut lire)
        projectMemberRepository.findByProjectAndUser(task.getProject(), currentUser)
                .orElseThrow(() -> new RuntimeException("You are not a member of this project"));

        return subTaskRepository.findByTask(task, pageable)
                .map(this::mapToDTO);
    }

    // UPDATE

    public SubTaskResponseDTO updateSubTask(Long subTaskId, SubTaskRequestDTO dto) {
        User currentUser = getCurrentUser();

        SubTask subTask = subTaskRepository.findById(subTaskId)
                .orElseThrow(() -> new RuntimeException("SubTask not found"));

        //  Vérifier que l'utilisateur est MEMBER ou ADMIN du projet
        ProjectMember member = projectMemberRepository.findByProjectAndUser(subTask.getTask().getProject(), currentUser)
                .orElseThrow(() -> new RuntimeException("You are not a member of this project"));

        if (member.getRole() == ProjectMember.ProjectRole.VIEWER) {
            throw new RuntimeException("Viewers can only read, not modify");
        }

        subTask.setTitle(dto.getTitle());
        subTask.setDescription(dto.getDescription());
        subTask.setStatus(dto.getStatus());

        SubTask saved = subTaskRepository.save(subTask);
        log.info("SubTask {} updated", subTaskId);

        //  Auto-update parent task status
        updateParentTaskStatus(saved.getTask().getId());

        return mapToDTO(saved);
    }

    // DELETE

    public void deleteSubTask(Long subTaskId) {
        User currentUser = getCurrentUser();

        SubTask subTask = subTaskRepository.findById(subTaskId)
                .orElseThrow(() -> new RuntimeException("SubTask not found"));

        //  Vérifier que l'utilisateur est MEMBER ou ADMIN du projet
        ProjectMember member = projectMemberRepository.findByProjectAndUser(subTask.getTask().getProject(), currentUser)
                .orElseThrow(() -> new RuntimeException("You are not a member of this project"));

        if (member.getRole() == ProjectMember.ProjectRole.VIEWER) {
            throw new RuntimeException("Viewers can only read, not modify");
        }

        Long taskId = subTask.getTask().getId();
        subTaskRepository.delete(subTask);
        log.info("SubTask {} deleted", subTaskId);

        //  Auto-update parent task status
        updateParentTaskStatus(taskId);
    }

    // HELPER - AUTO-UPDATE PARENT TASK STATUS

    private void updateParentTaskStatus(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        var subTasks = subTaskRepository.findByTask(task);

        if (subTasks.isEmpty()) {
            task.setStatus(Status.PENDING);
        } else {
            boolean allCompleted = subTasks.stream()
                    .allMatch(sub -> sub.getStatus() == Status.COMPLETED);

            if (allCompleted) {
                task.setStatus(Status.COMPLETED);
            } else {
                task.setStatus(Status.IN_PROGRESS);
            }
        }

        taskRepository.save(task);
        log.debug("Parent task {} status updated", taskId);
    }
}