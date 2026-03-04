package com.duva.taskflow.controller;

import com.duva.taskflow.dto.TaskResponseDTO;
import com.duva.taskflow.entity.enums.Priority;
import com.duva.taskflow.entity.enums.Status;
import com.duva.taskflow.service.TaskSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * TaskSearchController - API de recherche et filtrage avancé
 *
 * Endpoints:
 * - GET /api/workspaces/{wId}/projects/{pId}/tasks/search?q=...
 * - GET /api/workspaces/{wId}/projects/{pId}/tasks/filter/status?status=...
 * - GET /api/workspaces/{wId}/projects/{pId}/tasks/filter/priority?priority=...
 * - GET /api/workspaces/{wId}/projects/{pId}/tasks/filter/due-date?before=...
 * - GET /api/workspaces/{wId}/projects/{pId}/tasks/filter/assigned?to=...
 * - GET /api/workspaces/{wId}/projects/{pId}/tasks/filter/unassigned
 * - GET /api/workspaces/{wId}/projects/{pId}/tasks/search/advanced?q=...&status=...&priority=...
 */
@RestController
@RequestMapping("/api/workspaces/{workspaceId}/projects/{projectId}/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskSearchController {

    private final TaskSearchService taskSearchService;

    // SEARCH BY TEXT

    /**
     * Recherche par titre ou description
     * Paramètres: ?q=texte&page=0&size=20
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<TaskResponseDTO>> searchTasks(
            @PathVariable Long workspaceId,
            @PathVariable Long projectId,
            @RequestParam String q,
            Pageable pageable,
            Authentication authentication) {

        String email = authentication.getName();
        Page<TaskResponseDTO> results = taskSearchService.searchTasks(projectId, email, q, pageable);

        return ResponseEntity.ok(results);
    }

    // FILTER BY STATUS

    /**
     * Filtrer par statut
     * Paramètres: ?status=PENDING&page=0&size=20
     */
    @GetMapping("/filter/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<TaskResponseDTO>> filterByStatus(
            @PathVariable Long workspaceId,
            @PathVariable Long projectId,
            @RequestParam Status status,
            Pageable pageable,
            Authentication authentication) {

        String email = authentication.getName();
        Page<TaskResponseDTO> results = taskSearchService.filterByStatus(projectId, email, status, pageable);

        return ResponseEntity.ok(results);
    }

    // FILTER BY PRIORITY

    /**
     * Filtrer par priorité
     * Paramètres: ?priority=HIGH&page=0&size=20
     */
    @GetMapping("/filter/priority")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<TaskResponseDTO>> filterByPriority(
            @PathVariable Long workspaceId,
            @PathVariable Long projectId,
            @RequestParam Priority priority,
            Pageable pageable,
            Authentication authentication) {

        String email = authentication.getName();
        Page<TaskResponseDTO> results = taskSearchService.filterByPriority(projectId, email, priority, pageable);

        return ResponseEntity.ok(results);
    }

    // FILTER BY DUE DATE
    /**
     * Filtrer par date limite (avant une date)
     * Paramètres: ?before=2026-03-10&page=0&size=20
     */
    @GetMapping("/filter/due-date/before")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<TaskResponseDTO>> filterByDueDateBefore(
            @PathVariable Long workspaceId,
            @PathVariable Long projectId,
            @RequestParam LocalDate before,
            Pageable pageable,
            Authentication authentication) {

        String email = authentication.getName();
        Page<TaskResponseDTO> results = taskSearchService.filterByDueDateBefore(projectId, email, before, pageable);

        return ResponseEntity.ok(results);
    }

    /**
     * Filtrer par date limite (après une date)
     * Paramètres: ?after=2026-03-01&page=0&size=20
     */
    @GetMapping("/filter/due-date/after")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<TaskResponseDTO>> filterByDueDateAfter(
            @PathVariable Long workspaceId,
            @PathVariable Long projectId,
            @RequestParam LocalDate after,
            Pageable pageable,
            Authentication authentication) {

        String email = authentication.getName();
        Page<TaskResponseDTO> results = taskSearchService.filterByDueDateAfter(projectId, email, after, pageable);

        return ResponseEntity.ok(results);
    }

    // FILTER BY ASSIGNED USER

    /**
     * Filtrer les tâches assignées à un utilisateur
     * Paramètres: ?to=user@example.com&page=0&size=20
     */
    @GetMapping("/filter/assigned")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<TaskResponseDTO>> filterByAssignedTo(
            @PathVariable Long workspaceId,
            @PathVariable Long projectId,
            @RequestParam String to,
            Pageable pageable,
            Authentication authentication) {

        String email = authentication.getName();
        Page<TaskResponseDTO> results = taskSearchService.filterByAssignedTo(projectId, email, to, pageable);

        return ResponseEntity.ok(results);
    }

    /**
     * Filtrer les tâches non assignées
     */
    @GetMapping("/filter/unassigned")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<TaskResponseDTO>> filterUnassigned(
            @PathVariable Long workspaceId,
            @PathVariable Long projectId,
            Pageable pageable,
            Authentication authentication) {

        String email = authentication.getName();
        Page<TaskResponseDTO> results = taskSearchService.filterUnassigned(projectId, email, pageable);

        return ResponseEntity.ok(results);
    }

    // ADVANCED SEARCH (COMBINING FILTERS)

    /**
     * Recherche avancée combinant texte + statut + priorité
     * Paramètres: ?q=bug&status=PENDING&priority=HIGH&page=0&size=20
     */
    @GetMapping("/search/advanced")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<TaskResponseDTO>> advancedSearch(
            @PathVariable Long workspaceId,
            @PathVariable Long projectId,
            @RequestParam String q,
            @RequestParam Status status,
            @RequestParam Priority priority,
            Pageable pageable,
            Authentication authentication) {

        String email = authentication.getName();
        Page<TaskResponseDTO> results = taskSearchService.advancedSearch(projectId, email, q, status, priority, pageable);

        return ResponseEntity.ok(results);
    }
}