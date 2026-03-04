package com.duva.taskflow.service;

import com.duva.taskflow.entity.*;
import com.duva.taskflow.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;

/**
 * ProjectService - Logique métier des projets
 *
 * Responsabilités:
 * - Créer/modifier/supprimer des projets
 * - Gérer les membres du projet et leurs rôles
 * - Vérifier les permissions
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;

    // PROJECT OPERATIONS

    @Transactional
    public Project createProject(Long workspaceId, String email, String name, String description) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //  Vérifier que l'utilisateur est membre du workspace
        workspaceMemberRepository.findByWorkspaceAndUser(workspace, user)
                .orElseThrow(() -> new RuntimeException("You are not a member of this workspace"));

        Project project = Project.builder()
                .name(name)
                .description(description)
                .workspace(workspace)
                .createdBy(user)
                .build();

        project = projectRepository.save(project);

        //  Ajoute le créateur comme ADMIN du projet
        ProjectMember adminMember = ProjectMember.builder()
                .project(project)
                .user(user)
                .role(ProjectMember.ProjectRole.ADMIN)
                .build();

        projectMemberRepository.save(adminMember);

        log.info("Project created: {} in workspace {} by user: {}", project.getId(), workspaceId, email);
        return project;
    }

    public Project getProjectById(Long projectId, String email) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //  Vérifier que l'utilisateur est membre du projet
        checkProjectMembership(project, user);

        return project;
    }

    public Page<Project> getWorkspaceProjects(Long workspaceId, String email, Pageable pageable) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Vérifier que l'utilisateur est membre du workspace
        workspaceMemberRepository.findByWorkspaceAndUser(workspace, user)
                .orElseThrow(() -> new RuntimeException("You are not a member of this workspace"));

        return projectRepository.findUserProjectsInWorkspace(workspace, user, pageable);
    }

    @Transactional
    public Project updateProject(Long projectId, String email, String name, String description) {
        Project project = getProjectById(projectId, email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //  Seul l'ADMIN peut modifier
        checkProjectAdmin(project, user);

        project.setName(name);
        project.setDescription(description);
        project.setUpdatedAt(LocalDateTime.now());

        log.info("Project {} updated by user: {}", projectId, email);
        return projectRepository.save(project);
    }

    @Transactional
    public void deleteProject(Long projectId, String email) {
        Project project = getProjectById(projectId, email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //  Seul le créateur peut supprimer
        if (!project.getCreatedBy().getId().equals(user.getId())) {
            throw new RuntimeException("Only project creator can delete it");
        }

        projectRepository.delete(project);
        log.info("Project {} deleted by user: {}", projectId, email);
    }

    // MEMBER MANAGEMENT

    @Transactional
    public ProjectMember addMember(Long projectId, String currentUserEmail, String newMemberEmail, ProjectMember.ProjectRole role) {
        Project project = getProjectById(projectId, currentUserEmail);

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User newMember = userRepository.findByEmail(newMemberEmail)
                .orElseThrow(() -> new RuntimeException("Member to add not found"));

        //  Seul l'ADMIN peut ajouter des membres
        checkProjectAdmin(project, currentUser);

        // Vérifie qu'il n'est pas déjà membre
        if (projectMemberRepository.findByProjectAndUser(project, newMember).isPresent()) {
            throw new RuntimeException("User is already a member of this project");
        }

        ProjectMember member = ProjectMember.builder()
                .project(project)
                .user(newMember)
                .role(role)
                .build();

        log.info("Member {} added to project {} with role {}", newMemberEmail, projectId, role);
        return projectMemberRepository.save(member);
    }

    @Transactional
    public void removeMember(Long projectId, String currentUserEmail, String memberEmailToRemove) {
        Project project = getProjectById(projectId, currentUserEmail);

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User memberToRemove = userRepository.findByEmail(memberEmailToRemove)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        //  Seul l'ADMIN peut supprimer des membres
        checkProjectAdmin(project, currentUser);

        projectMemberRepository.deleteByProjectAndUser(project, memberToRemove);
        log.info("Member {} removed from project {}", memberEmailToRemove, projectId);
    }

    // PERMISSION CHECKS

    public void checkProjectMembership(Project project, User user) {
        boolean isMember = projectRepository.isMember(project.getId(), user);
        if (!isMember) {
            throw new RuntimeException("You are not a member of this project");
        }
    }

    public void checkProjectAdmin(Project project, User user) {
        ProjectMember member = projectMemberRepository.findByProjectAndUser(project, user)
                .orElseThrow(() -> new RuntimeException("You are not a member of this project"));

        if (member.getRole() != ProjectMember.ProjectRole.ADMIN) {
            throw new RuntimeException("You don't have admin permission in this project");
        }
    }

    public void checkProjectMemberOrHigher(Project project, User user) {
        ProjectMember member = projectMemberRepository.findByProjectAndUser(project, user)
                .orElseThrow(() -> new RuntimeException("You are not a member of this project"));

        if (member.getRole() == ProjectMember.ProjectRole.VIEWER) {
            throw new RuntimeException("Viewers can only read, not modify");
        }
    }

    public ProjectMember.ProjectRole getUserProjectRole(Long projectId, String email) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return projectMemberRepository.findByProjectAndUser(project, user)
                .orElseThrow(() -> new RuntimeException("User is not a member of this project"))
                .getRole();
    }
}