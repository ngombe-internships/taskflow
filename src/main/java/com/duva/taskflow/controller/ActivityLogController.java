package com.duva.taskflow.controller;

import com.duva.taskflow.dto.ActivityLogResponseDTO;
import com.duva.taskflow.entity.ActivityLog;
import com.duva.taskflow.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * ActivityLogController - Gestion de l'historique des tâches
 *
 * Endpoints:
 * - GET /api/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}/activity
 * - GET /api/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}/activity?type=STATUS_CHANGED
 */
@RestController
@RequestMapping("/api/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}/activity")
@RequiredArgsConstructor
@Slf4j
public class ActivityLogController {

    private final ActivityLogService activityLogService;

     //Récupère l'historique complet d'une tâche (paginé)
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ActivityLogResponseDTO>> getActivityLog(
            @PathVariable Long workspaceId,
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            Pageable pageable,
            Authentication authentication) {

        String email = authentication.getName();
        Page<ActivityLogResponseDTO> logs = activityLogService.getTaskActivityLog(projectId, taskId, email, pageable);

        return ResponseEntity.ok(logs);
    }

    /**
     * Récupère l'historique filtré par type d'action
     *
     * Paramètre: ?type=STATUS_CHANGED
     * Types disponibles: TASK_CREATED, TASK_UPDATED, STATUS_CHANGED, PRIORITY_CHANGED,
     *                    ASSIGNED, UNASSIGNED, COMMENT_ADDED, COMMENT_DELETED,
     *                    DUE_DATE_CHANGED, DESCRIPTION_CHANGED, TASK_DELETED
     */
    @GetMapping("/filter")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ActivityLogResponseDTO>> getActivityLogByType(
            @PathVariable Long workspaceId,
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestParam ActivityLog.ActionType type,
            Pageable pageable,
            Authentication authentication) {

        String email = authentication.getName();
        Page<ActivityLogResponseDTO> logs = activityLogService.getTaskActivityByType(projectId, taskId, email, type, pageable);

        return ResponseEntity.ok(logs);
    }
}