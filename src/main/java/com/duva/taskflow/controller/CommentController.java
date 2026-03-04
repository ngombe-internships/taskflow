package com.duva.taskflow.controller;

import com.duva.taskflow.dto.CommentRequestDTO;
import com.duva.taskflow.dto.CommentResponseDTO;
import com.duva.taskflow.service.CommentService;
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
 * CommentController - Gestion des commentaires sur les tâches
 *
 * Endpoints:
 * - POST   /api/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}/comments
 * - GET    /api/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}/comments
 * - GET    /api/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}/comments/{commentId}
 * - PUT    /api/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}/comments/{commentId}
 * - DELETE /api/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}/comments/{commentId}
 */
@RestController
@RequestMapping("/api/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;


    // CREATE
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponseDTO> createComment(
            @PathVariable Long workspaceId,
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody CommentRequestDTO dto,
            Authentication authentication) {

        String email = authentication.getName();
        CommentResponseDTO createdComment = commentService.createComment(projectId, taskId, email, dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdComment);
    }

    // READ

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CommentResponseDTO>> getTaskComments(
            @PathVariable Long workspaceId,
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            Pageable pageable,
            Authentication authentication) {

        String email = authentication.getName();
        Page<CommentResponseDTO> comments = commentService.getTaskComments(projectId, taskId, email, pageable);

        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponseDTO> getCommentById(
            @PathVariable Long workspaceId,
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @PathVariable Long commentId,
            Authentication authentication) {

        String email = authentication.getName();
        CommentResponseDTO comment = commentService.getCommentById(projectId, taskId, commentId, email);

        return ResponseEntity.ok(comment);
    }

    // UPDATE

    @PutMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponseDTO> updateComment(
            @PathVariable Long workspaceId,
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequestDTO dto,
            Authentication authentication) {

        String email = authentication.getName();
        CommentResponseDTO updatedComment = commentService.updateComment(projectId, taskId, commentId, email, dto);

        return ResponseEntity.ok(updatedComment);
    }

    // DELETE

    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> deleteComment(
            @PathVariable Long workspaceId,
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @PathVariable Long commentId,
            Authentication authentication) {

        String email = authentication.getName();
        commentService.deleteComment(projectId, taskId, commentId, email);

        return ResponseEntity.ok("Comment deleted successfully");
    }
}