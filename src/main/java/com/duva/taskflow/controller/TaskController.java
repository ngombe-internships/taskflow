package com.duva.taskflow.controller;

import com.duva.taskflow.entity.Task;
import com.duva.taskflow.entity.enums.Status;
import com.duva.taskflow.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // Create a new task
    @PostMapping
    public Task createTask(@RequestBody Task task) {
        return taskService.createTask(task);
    }

    // Get paginated tasks of logged-in user
    @GetMapping
    public Page<Task> getMyTasks(Pageable pageable) {
        return taskService.getMyTasks(pageable);
    }

    // Get task by ID
    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    // Update task
    @PutMapping("/{id}")
    public Task updateTask(@PathVariable Long id,
                           @RequestBody Task task) {
        return taskService.updateTask(id, task);
    }

    // Get paginated tasks filtered by status
    @GetMapping("/status/{status}")
    public Page<Task> getTasksByStatus(@PathVariable Status status,
                                       Pageable pageable) {
        return taskService.getMyTasksByStatus(status, pageable);
    }

    // Delete task
    @DeleteMapping("/{id}")
    public String deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return "Task deleted successfully";
    }
}