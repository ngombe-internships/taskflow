package com.duva.taskflow.controller;

import com.duva.taskflow.dto.ProjectRequestDTO;
import com.duva.taskflow.dto.ProjectResponseDTO;
import com.duva.taskflow.dto.AddProjectMemberRequestDTO;
import com.duva.taskflow.service.ProjectService;
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
 * ProjectController - Gestion des projets
 *
 * Endpoints:
 * - POST   /api/workspaces/{workspaceId}/projects
 * - GET    /api/workspaces/{workspaceId}/projects
 * - GET    /api/workspaces/{workspaceId}/projects/{projectId}
 * - PUT    /api/workspaces/{workspaceId}/projects/{projectId}
 * - DELETE /api/workspaces/{workspaceId}/projects/{projectId}
 * - POST   /api/workspaces/{workspaceId}/projects/{projectId}/members
 * - DELETE /api/workspaces/{workspaceId}/projects/{projectId}/members/{email}
 */
@RestController
@RequestMapping("/api/workspaces/{workspaceId}/projects")
@RequiredArgsConstructor
@Slf4j
public class ProjectController {

    private final ProjectService projectService;


    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectResponseDTO> createProject(
            @PathVariable Long workspaceId,
            @Valid @RequestBody ProjectRequestDTO dto,
            Authentication authentication) {

        String email = authentication.getName();
        var project = projectService.createProject(workspaceId, email, dto.getName(), dto.getDescription());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapToDTO(project));
    }


    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ProjectResponseDTO>> getWorkspaceProjects(
            @PathVariable Long workspaceId,
            Pageable pageable,
            Authentication authentication) {

        String email = authentication.getName();
        Page<ProjectResponseDTO> projects = projectService
                .getWorkspaceProjects(workspaceId, email, pageable)
                .map(this::mapToDTO);

        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectResponseDTO> getProjectById(
            @PathVariable Long workspaceId,
            @PathVariable Long projectId,
            Authentication authentication) {

        String email = authentication.getName();
        var project = projectService.getProjectById(projectId, email);

        return ResponseEntity.ok(mapToDTO(project));
    }



    @PutMapping("/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectResponseDTO> updateProject(
            @PathVariable Long workspaceId,
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectRequestDTO dto,
            Authentication authentication) {

        String email = authentication.getName();
        var project = projectService.updateProject(projectId, email, dto.getName(), dto.getDescription());

        return ResponseEntity.ok(mapToDTO(project));
    }



    @DeleteMapping("/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> deleteProject(
            @PathVariable Long workspaceId,
            @PathVariable Long projectId,
            Authentication authentication) {

        String email = authentication.getName();
        projectService.deleteProject(projectId, email);

        return ResponseEntity.ok("Project deleted successfully");
    }



    @PostMapping("/{projectId}/members")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> addMember(
            @PathVariable Long workspaceId,
            @PathVariable Long projectId,
            @RequestBody AddProjectMemberRequestDTO request,
            Authentication authentication) {

        String email = authentication.getName();
        projectService.addMember(projectId, email, request.getEmail(), request.getRole());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Member added successfully");
    }

    @DeleteMapping("/{projectId}/members/{memberEmail}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> removeMember(
            @PathVariable Long workspaceId,
            @PathVariable Long projectId,
            @PathVariable String memberEmail,
            Authentication authentication) {

        String email = authentication.getName();
        projectService.removeMember(projectId, email, memberEmail);

        return ResponseEntity.ok("Member removed successfully");
    }

    // HELPER

    private ProjectResponseDTO mapToDTO(com.duva.taskflow.entity.Project project) {
        return ProjectResponseDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .workspaceId(project.getWorkspace().getId())
                .createdBy(project.getCreatedBy().getEmail())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}