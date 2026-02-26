package com.duva.taskflow.service;

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

    // Get currently authenticated user
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    // Create new task for logged-in user
    public Task createTask(Task task) {

        User currentUser = getCurrentUser();

        task.setUser(currentUser);
        task.setStatus(Status.PENDING);

        return taskRepository.save(task);
    }

    // Get paginated tasks of current user
    public Page<Task> getMyTasks(Pageable pageable) {
        User currentUser = getCurrentUser();
        return taskRepository.findByUser(currentUser, pageable);
    }

    // Get paginated tasks of current user filtered by status
    public Page<Task> getMyTasksByStatus(Status status, Pageable pageable) {
        User currentUser = getCurrentUser();
        return taskRepository.findByUserAndStatus(currentUser, status, pageable);
    }

    // Get task by id (only if owner or ADMIN)
    public Task getTaskById(Long id) {

        User currentUser = getCurrentUser();

        Task task = taskRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Task not found"));

        boolean isAdmin =
                currentUser.getRole().getName().equals("ROLE_ADMIN");

        if (!task.getUser().getId().equals(currentUser.getId()) && !isAdmin) {
            throw new RuntimeException("Access denied");
        }

        return task;
    }

    // Update task (owner or ADMIN)
    public Task updateTask(Long id, Task updatedTask) {

        Task existingTask = getTaskById(id);

        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setStartDate(updatedTask.getStartDate());
        existingTask.setDueDate(updatedTask.getDueDate());
        existingTask.setPriority(updatedTask.getPriority());
        existingTask.setStatus(updatedTask.getStatus());

        return taskRepository.save(existingTask);
    }

    // Delete task (owner or ADMIN)
    public void deleteTask(Long id) {

        Task task = getTaskById(id);
        taskRepository.delete(task);
    }
}