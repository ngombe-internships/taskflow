package com.duva.taskflow.service;

import com.duva.taskflow.dto.TaskRequestDTO;
import com.duva.taskflow.dto.TaskResponseDTO;
import com.duva.taskflow.entity.Task;
import com.duva.taskflow.entity.User;
import com.duva.taskflow.entity.enums.Status;
import com.duva.taskflow.repository.TaskRepository;
import com.duva.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    // Convert Task entity to Response DTO
    private TaskResponseDTO mapToDTO(Task task) {
        return TaskResponseDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .startDate(task.getStartDate())
                .dueDate(task.getDueDate())
                .status(task.getStatus())
                .priority(task.getPriority())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    // Get currently authenticated user
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    // Create new task
    public TaskResponseDTO createTask(TaskRequestDTO dto) {

        User currentUser = getCurrentUser();

        Task task = Task.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .startDate(dto.getStartDate())
                .dueDate(dto.getDueDate())
                .priority(dto.getPriority())
                .status(Status.PENDING)
                .user(currentUser)
                .build();

        Task savedTask = taskRepository.save(task);

        return mapToDTO(savedTask);
    }

    // Get paginated tasks of current user
    public Page<TaskResponseDTO> getMyTasks(Pageable pageable) {

        User currentUser = getCurrentUser();

        return taskRepository.findByUser(currentUser, pageable)
                .map(this::mapToDTO);
    }

    // Get paginated tasks filtered by status
    public Page<TaskResponseDTO> getMyTasksByStatus(Status status, Pageable pageable) {

        User currentUser = getCurrentUser();

        return taskRepository.findByUserAndStatus(currentUser, status, pageable)
                .map(this::mapToDTO);
    }

    // Get task by ID (owner or ADMIN)
    public TaskResponseDTO getTaskById(Long id) {

        User currentUser = getCurrentUser();

        Task task = taskRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Task not found"));

        boolean isAdmin =
                currentUser.getRole().getName().equals("ROLE_ADMIN");

        if (!task.getUser().getId().equals(currentUser.getId()) && !isAdmin) {
            throw new RuntimeException("Access denied");
        }

        return mapToDTO(task);
    }

    // Update task (owner or ADMIN)
    public TaskResponseDTO updateTask(Long id, TaskRequestDTO dto) {

        User currentUser = getCurrentUser();

        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Task not found"));

        boolean isAdmin =
                currentUser.getRole().getName().equals("ROLE_ADMIN");

        if (!existingTask.getUser().getId().equals(currentUser.getId()) && !isAdmin) {
            throw new RuntimeException("Access denied");
        }

        existingTask.setTitle(dto.getTitle());
        existingTask.setDescription(dto.getDescription());
        existingTask.setStartDate(dto.getStartDate());
        existingTask.setDueDate(dto.getDueDate());
        existingTask.setPriority(dto.getPriority());
        existingTask.setStatus(dto.getStatus());

        Task saved = taskRepository.save(existingTask);

        return mapToDTO(saved);
    }

    // Delete task (owner or ADMIN)
    public void deleteTask(Long id) {

        User currentUser = getCurrentUser();

        Task task = taskRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Task not found"));

        boolean isAdmin =
                currentUser.getRole().getName().equals("ROLE_ADMIN");

        if (!task.getUser().getId().equals(currentUser.getId()) && !isAdmin) {
            throw new RuntimeException("Access denied");
        }

        taskRepository.delete(task);
    }
}