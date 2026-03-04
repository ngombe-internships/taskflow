package com.duva.taskflow.service;

import com.duva.taskflow.dto.TaskRequestDTO;
import com.duva.taskflow.dto.TaskResponseDTO;
import com.duva.taskflow.entity.*;
import com.duva.taskflow.entity.enums.Status;
import com.duva.taskflow.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;

/**
 * TaskService - Logique métier des tâches
 *
 * Responsabilités:
 * - Créer/lire/mettre à jour/supprimer les tâches dans un projet
 * - Vérifier les permissions du projet
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final ProjectService projectService;

    // CREATE

    @Transactional
    public TaskResponseDTO createTask(Long projectId, String email, TaskRequestDTO dto) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //  Vérifier que l'utilisateur est MEMBER ou ADMIN du projet
        projectService.checkProjectMemberOrHigher(project, user);

        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus() != null ? dto.getStatus() : Status.PENDING);
        task.setPriority(dto.getPriority());
        task.setStartDate(dto.getStartDate());
        task.setDueDate(dto.getDueDate());
        task.setProject(project);
        task.setCreatedBy(user);

        Task savedTask = taskRepository.save(task);
        log.info("Task created: {} in project {} by user: {}", savedTask.getId(), projectId, email);

        return mapToDTO(savedTask);
    }

    // READ

    public Page<TaskResponseDTO> getProjectTasks(Long projectId, String email, Pageable pageable) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //  Vérifier que l'utilisateur est membre du projet
        projectService.checkProjectMembership(project, user);

        Page<Task> tasks = taskRepository.findByProject(project, pageable);
        log.debug("Found {} tasks in project {}", tasks.getTotalElements(), projectId);
        return tasks.map(this::mapToDTO);
    }

    public TaskResponseDTO getTaskById(Long projectId, Long taskId, String email) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //  Vérifier que l'utilisateur est membre du projet
        projectService.checkProjectMembership(project, user);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        //  Vérifier que la tâche appartient au projet
        if (!task.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Task does not belong to this project");
        }

        return mapToDTO(task);
    }

    public Page<TaskResponseDTO> getTasksByStatus(Long projectId, String email, Status status, Pageable pageable) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //  Vérifier que l'utilisateur est membre du projet
        projectService.checkProjectMembership(project, user);

        Page<Task> tasks = taskRepository.findByProjectAndStatus(project, status, pageable);
        log.debug("Found {} tasks with status {} in project {}", tasks.getTotalElements(), status, projectId);
        return tasks.map(this::mapToDTO);
    }

    // UPDATE

    @Transactional
    public TaskResponseDTO updateTask(Long projectId, Long taskId, String email, TaskRequestDTO dto) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //  Vérifier permissions
        projectService.checkProjectMemberOrHigher(project, user);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        //  Vérifier que la tâche appartient au projet
        if (!task.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Task does not belong to this project");
        }

        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            task.setTitle(dto.getTitle());
        }

        if (dto.getDescription() != null) {
            task.setDescription(dto.getDescription());
        }

        if (dto.getStatus() != null) {
            task.setStatus(dto.getStatus());
        }

        if (dto.getPriority() != null) {
            task.setPriority(dto.getPriority());
        }

        if (dto.getStartDate() != null) {
            task.setStartDate(dto.getStartDate());
        }

        if (dto.getDueDate() != null) {
            task.setDueDate(dto.getDueDate());
        }

        task.setUpdatedAt(LocalDateTime.now());
        Task updatedTask = taskRepository.save(task);

        log.info("Task {} updated in project {} by user: {}", taskId, projectId, email);
        return mapToDTO(updatedTask);
    }

    // DELETE

    @Transactional
    public void deleteTask(Long projectId, Long taskId, String email) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //  Vérifier permissions
        projectService.checkProjectMemberOrHigher(project, user);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        //  Vérifier que la tâche appartient au projet
        if (!task.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Task does not belong to this project");
        }

        taskRepository.deleteById(taskId);
        log.info("Task {} deleted from project {} by user: {}", taskId, projectId, email);
    }

    // HELPER - MAPPING

    private TaskResponseDTO mapToDTO(Task task) {
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