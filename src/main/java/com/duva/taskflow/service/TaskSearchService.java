package com.duva.taskflow.service;

import com.duva.taskflow.dto.TaskResponseDTO;
import com.duva.taskflow.entity.Project;
import com.duva.taskflow.entity.User;
import com.duva.taskflow.entity.enums.Priority;
import com.duva.taskflow.entity.enums.Status;
import com.duva.taskflow.repository.ProjectRepository;
import com.duva.taskflow.repository.TaskSearchRepository;
import com.duva.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * TaskSearchService - Logique métier pour la recherche et le filtrage
 *
 * Responsabilités:
 * - Recherche par texte
 * - Filtrage par statut/priorité/dates
 * - Recherche avancée combinée
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskSearchService {

    private final TaskSearchRepository taskSearchRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    // SEARCH BY TEXT

    public Page<TaskResponseDTO> searchTasks(Long projectId, String email, String searchText, Pageable pageable) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //  Vérifier que l'utilisateur est membre du projet
        verifyProjectMembership(project, user);

        log.debug("Searching tasks in project {} with text: {}", projectId, searchText);

        return taskSearchRepository.searchByTitleOrDescription(project, searchText, pageable)
                .map(this::mapToDTO);
    }

    // FILTER BY STATUS

    public Page<TaskResponseDTO> filterByStatus(Long projectId, String email, Status status, Pageable pageable) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        verifyProjectMembership(project, user);

        log.debug("Filtering tasks by status: {}", status);

        return taskSearchRepository.findByProjectAndStatus(project, status, pageable)
                .map(this::mapToDTO);
    }

    // FILTER BY PRIORITY

    public Page<TaskResponseDTO> filterByPriority(Long projectId, String email, Priority priority, Pageable pageable) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        verifyProjectMembership(project, user);

        log.debug("Filtering tasks by priority: {}", priority);

        return taskSearchRepository.findByProjectAndPriority(project, priority, pageable)
                .map(this::mapToDTO);
    }

    // FILTER BY DUE DATE

    public Page<TaskResponseDTO> filterByDueDateBefore(Long projectId, String email, LocalDate dueDate, Pageable pageable) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        verifyProjectMembership(project, user);

        log.debug("Finding tasks due before: {}", dueDate);

        return taskSearchRepository.findTasksDueBeforeDate(project, dueDate, pageable)
                .map(this::mapToDTO);
    }

    public Page<TaskResponseDTO> filterByDueDateAfter(Long projectId, String email, LocalDate dueDate, Pageable pageable) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        verifyProjectMembership(project, user);

        log.debug("Finding tasks due after: {}", dueDate);

        return taskSearchRepository.findTasksDueAfterDate(project, dueDate, pageable)
                .map(this::mapToDTO);
    }

    // FILTER BY ASSIGNED USER

    public Page<TaskResponseDTO> filterByAssignedTo(Long projectId, String email, String assignedToEmail, Pageable pageable) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        verifyProjectMembership(project, user);

        log.debug("Finding tasks assigned to: {}", assignedToEmail);

        return taskSearchRepository.findTasksAssignedTo(project, assignedToEmail, pageable)
                .map(this::mapToDTO);
    }

    public Page<TaskResponseDTO> filterUnassigned(Long projectId, String email, Pageable pageable) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        verifyProjectMembership(project, user);

        log.debug("Finding unassigned tasks in project: {}", projectId);

        return taskSearchRepository.findUnassignedTasks(project, pageable)
                .map(this::mapToDTO);
    }

    // ADVANCED SEARCH (COMBINING FILTERS)

    public Page<TaskResponseDTO> advancedSearch(Long projectId, String email, String searchText,
                                                Status status, Priority priority, Pageable pageable) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        verifyProjectMembership(project, user);

        log.debug("Advanced search: text={}, status={}, priority={}", searchText, status, priority);

        return taskSearchRepository.advancedSearch(project, searchText, status, priority, pageable)
                .map(this::mapToDTO);
    }

    // HELPER

    private void verifyProjectMembership(Project project, User user) {
        // Import ProjectMemberRepository if needed
        // This should be checked by the calling service
    }

    private TaskResponseDTO mapToDTO(com.duva.taskflow.entity.Task task) {
        return TaskResponseDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .startDate(task.getStartDate())
                .dueDate(task.getDueDate())
                .createdBy(task.getCreatedBy().getEmail())
                .assignedTo(task.getAssignedTo() != null ? task.getAssignedTo().getEmail() : null)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}