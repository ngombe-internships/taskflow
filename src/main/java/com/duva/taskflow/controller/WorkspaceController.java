package com.duva.taskflow.controller;

import com.duva.taskflow.dto.WorkspaceRequestDTO;
import com.duva.taskflow.dto.WorkspaceResponseDTO;
import com.duva.taskflow.dto.AddMemberRequestDTO;
import com.duva.taskflow.service.WorkspaceService;
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
 * WorkspaceController - Gestion des workspaces (équipes)
 *
 * Endpoints:
 * - POST   /api/workspaces              - Créer un workspace
 * - GET    /api/workspaces              - Lister mes workspaces
 * - GET    /api/workspaces/{id}         - Détail d'un workspace
 * - PUT    /api/workspaces/{id}         - Modifier un workspace
 * - DELETE /api/workspaces/{id}         - Supprimer un workspace
 * - POST   /api/workspaces/{id}/members - Ajouter un membre
 * - DELETE /api/workspaces/{id}/members/{email} - Supprimer un membre
 */
@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
@Slf4j
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    // CREATE
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WorkspaceResponseDTO> createWorkspace(
            @Valid @RequestBody WorkspaceRequestDTO dto,
            Authentication authentication) {

        String email = authentication.getName();
        var workspace = workspaceService.createWorkspace(email, dto.getName(), dto.getDescription());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapToDTO(workspace));
    }

    // READ
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<WorkspaceResponseDTO>> getUserWorkspaces(
            Pageable pageable,
            Authentication authentication) {

        String email = authentication.getName();
        Page<WorkspaceResponseDTO> workspaces = workspaceService
                .getUserWorkspaces(email, pageable)
                .map(this::mapToDTO);

        return ResponseEntity.ok(workspaces);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WorkspaceResponseDTO> getWorkspaceById(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();
        var workspace = workspaceService.getWorkspaceById(id, email);

        return ResponseEntity.ok(mapToDTO(workspace));
    }

    // UPDATE
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WorkspaceResponseDTO> updateWorkspace(
            @PathVariable Long id,
            @Valid @RequestBody WorkspaceRequestDTO dto,
            Authentication authentication) {

        String email = authentication.getName();
        var workspace = workspaceService.updateWorkspace(id, email, dto.getName(), dto.getDescription());

        return ResponseEntity.ok(mapToDTO(workspace));
    }

    // DELETE
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> deleteWorkspace(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();
        workspaceService.deleteWorkspace(id, email);

        return ResponseEntity.ok("Workspace deleted successfully");
    }

    // MEMBERS
    @PostMapping("/{id}/members")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> addMember(
            @PathVariable Long id,
            @RequestBody AddMemberRequestDTO request,
            Authentication authentication) {

        String email = authentication.getName();
        workspaceService.addMember(id, email, request.getEmail(), request.getRole());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Member added successfully");
    }

    @DeleteMapping("/{id}/members/{memberEmail}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> removeMember(
            @PathVariable Long id,
            @PathVariable String memberEmail,
            Authentication authentication) {

        String email = authentication.getName();
        workspaceService.removeMember(id, email, memberEmail);

        return ResponseEntity.ok("Member removed successfully");
    }

    // HELPER
    private WorkspaceResponseDTO mapToDTO(com.duva.taskflow.entity.Workspace workspace) {
        return WorkspaceResponseDTO.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .description(workspace.getDescription())
                .createdBy(workspace.getCreatedBy().getEmail())
                .createdAt(workspace.getCreatedAt())
                .updatedAt(workspace.getUpdatedAt())
                .build();
    }
}