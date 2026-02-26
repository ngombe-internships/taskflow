package com.duva.taskflow.service;

import com.duva.taskflow.dto.SubTaskRequestDTO;
import com.duva.taskflow.dto.SubTaskResponseDTO;
import com.duva.taskflow.entity.SubTask;
import com.duva.taskflow.entity.Task;
import com.duva.taskflow.entity.User;
import com.duva.taskflow.entity.enums.Status;
import com.duva.taskflow.repository.SubTaskRepository;
import com.duva.taskflow.repository.TaskRepository;
import com.duva.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SubTaskService {

    private final SubTaskRepository subTaskRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    // Get authenticated user
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    // Check if user has ADMIN role
    private boolean isAdmin(User user) {
        return user.getRole().getName().equals("ROLE_ADMIN");
    }

    // Convert entity to DTO
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

    // Verify ownership of parent task
    private Task verifyTaskOwnership(Long taskId) {

        User currentUser = getCurrentUser();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new RuntimeException("Task not found"));

        if (!task.getUser().getId().equals(currentUser.getId())
                && !isAdmin(currentUser)) {
            throw new RuntimeException("Access denied");
        }

        return task;
    }

    // Create subtask
    public SubTaskResponseDTO createSubTask(Long taskId, SubTaskRequestDTO dto) {

        Task task = verifyTaskOwnership(taskId);

        SubTask subTask = SubTask.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(Status.PENDING)
                .task(task)
                .build();

        SubTask saved = subTaskRepository.save(subTask);

        // Optional: recalculate after creation
        updateParentTaskStatus(taskId);

        return mapToDTO(saved);
    }

    // Get paginated subtasks
    public Page<SubTaskResponseDTO> getSubTasks(Long taskId, Pageable pageable) {

        Task task = verifyTaskOwnership(taskId);

        return subTaskRepository.findByTask(task, pageable)
                .map(this::mapToDTO);
    }

    // Update subtask
    public SubTaskResponseDTO updateSubTask(Long subTaskId, SubTaskRequestDTO dto) {

        User currentUser = getCurrentUser();

        SubTask subTask = subTaskRepository.findById(subTaskId)
                .orElseThrow(() ->
                        new RuntimeException("SubTask not found"));

        if (!subTask.getTask().getUser().getId().equals(currentUser.getId())
                && !isAdmin(currentUser)) {
            throw new RuntimeException("Access denied");
        }

        subTask.setTitle(dto.getTitle());
        subTask.setDescription(dto.getDescription());
        subTask.setStatus(dto.getStatus());

        SubTask saved = subTaskRepository.save(subTask);

        // Auto update parent task
        updateParentTaskStatus(saved.getTask().getId());

        return mapToDTO(saved);
    }

    // Delete subtask
    public void deleteSubTask(Long subTaskId) {

        User currentUser = getCurrentUser();

        SubTask subTask = subTaskRepository.findById(subTaskId)
                .orElseThrow(() ->
                        new RuntimeException("SubTask not found"));

        if (!subTask.getTask().getUser().getId().equals(currentUser.getId())
                && !isAdmin(currentUser)) {
            throw new RuntimeException("Access denied");
        }

        Long taskId = subTask.getTask().getId();

        subTaskRepository.delete(subTask);

        // Recalculate parent after deletion
        updateParentTaskStatus(taskId);
    }

    // Auto-update parent task status
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
    }
}