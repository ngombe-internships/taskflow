package com.duva.taskflow.controller;

import com.duva.taskflow.dto.TaskRequestDTO;
import com.duva.taskflow.dto.TaskResponseDTO;
import com.duva.taskflow.entity.enums.Status;
import com.duva.taskflow.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * TaskController - Gestion des tâches dans les projets
 *
 * Endpoints:
 * - POST   /api/workspaces/{workspaceId}/projects/{projectId}/tasks
 * - GET    /api/workspaces/{workspaceId}/projects/{projectId}/tasks
 * - GET    /api/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}
 * - PUT    /api/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}
 * - GET    /api/workspaces/{workspaceId}/projects/{projectId}/tasks/status/{status}
 * - DELETE /api/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}
 */
@RestController
@RequestMapping("/api/workspaces/{workspaceId}/projects/{projectId}/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {

    private final TaskService taskService;

    // CREATE
    /**
     * Crée une nouvelle tâche dans un projet
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskResponseDTO> createTask(
            @PathVariable Long workspaceId,
            @PathVariable Long projectId,
            @Valid @RequestBody TaskRequestDTO dto,
            Authentication authentication) {

        String email = authentication.getName();
        TaskResponseDTO createdTask = taskService.createTask(projectId, email, dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdTask);
    }

    // READ - GET ALL TASKS IN PROJECT

    /**
     * Récupère toutes les tâches d'un projet (paginé)
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<TaskResponseDTO>> getProjectTasks(
            @PathVariable Long workspaceId,
            @PathVariable Long projectId,
            Pageable pageable,
            Authentication authentication) {

        String email = authentication.getName();
        Page<TaskResponseDTO> tasks = taskService.getProjectTasks(projectId, email, pageable);

        return ResponseEntity.ok(tasks);
    }

    // READ - GET TASK BY ID

    /**
     * Récupère une tâche spécifique par son ID
     */
    @GetMapping("/{taskId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskResponseDTO> getTaskById(
            @PathVariable Long workspaceId,
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            Authentication authentication) {

        String email = authentication.getName();
        TaskResponseDTO task = taskService.getTaskById(projectId, taskId, email);

        return ResponseEntity.ok(task);
    }

    // READ - GET TASKS BY STATUS

    /**
     * Récupère les tâches d'un projet filtrées par statut (paginé)
     *
     * Statuts valides: PENDING, IN_PROGRESS, COMPLETED
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<TaskResponseDTO>> getTasksByStatus(
            @PathVariable Long workspaceId,
            @PathVariable Long projectId,
            @PathVariable Status status,
            Pageable pageable,
            Authentication authentication) {

        String email = authentication.getName();
        Page<TaskResponseDTO> tasks = taskService.getTasksByStatus(projectId, email, status, pageable);

        return ResponseEntity.ok(tasks);
    }

    // UPDATE

    /**
     * Met à jour une tâche existante
     */
    @PutMapping("/{taskId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskResponseDTO> updateTask(
            @PathVariable Long workspaceId,
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskRequestDTO dto,
            Authentication authentication) {

        String email = authentication.getName();
        TaskResponseDTO updatedTask = taskService.updateTask(projectId, taskId, email, dto);

        return ResponseEntity.ok(updatedTask);
    }

    // DELETE

    /**
     * Supprime une tâche
     */
    @DeleteMapping("/{taskId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> deleteTask(
            @PathVariable Long workspaceId,
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            Authentication authentication) {

        String email = authentication.getName();
        taskService.deleteTask(projectId, taskId, email);

        return ResponseEntity.ok("Task deleted successfully");
    }
}