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
 * WorkspaceService - Logique métier des workspaces
 *
 * Responsabilités:
 * - Créer/modifier/supprimer des workspaces
 * - Gérer les membres et leurs rôles
 * - Vérifier les permissions
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;

    // WORKSPACE OPERATIONS

    @Transactional
    public Workspace createWorkspace(String email, String name, String description) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Workspace workspace = Workspace.builder()
                .name(name)
                .description(description)
                .createdBy(user)
                .build();

        workspace = workspaceRepository.save(workspace);

        // Ajoute le créateur comme ADMIN
        WorkspaceMember adminMember = WorkspaceMember.builder()
                .workspace(workspace)
                .user(user)
                .role(WorkspaceMember.WorkspaceRole.ADMIN)
                .build();

        workspaceMemberRepository.save(adminMember);

        log.info("Workspace created: {} by user: {}", workspace.getId(), email);
        return workspace;
    }

    public Workspace getWorkspaceById(Long workspaceId, String email) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace not found with id: " + workspaceId));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Vérifier que l'utilisateur est membre
        checkWorkspaceMembership(workspace, user);

        return workspace;
    }

    public Page<Workspace> getUserWorkspaces(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return workspaceRepository.findUserWorkspaces(user, pageable);
    }

    @Transactional
    public Workspace updateWorkspace(Long workspaceId, String email, String name, String description) {
        Workspace workspace = getWorkspaceById(workspaceId, email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //  Seul l'ADMIN peut modifier
        checkWorkspaceAdmin(workspace, user);

        workspace.setName(name);
        workspace.setDescription(description);
        workspace.setUpdatedAt(LocalDateTime.now());

        log.info("Workspace {} updated by user: {}", workspaceId, email);
        return workspaceRepository.save(workspace);
    }

    @Transactional
    public void deleteWorkspace(Long workspaceId, String email) {
        Workspace workspace = getWorkspaceById(workspaceId, email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //  Seul l'ADMIN (créateur) peut supprimer
        if (!workspace.getCreatedBy().getId().equals(user.getId())) {
            throw new RuntimeException("Only workspace creator can delete it");
        }

        workspaceRepository.delete(workspace);
        log.info("Workspace {} deleted by user: {}", workspaceId, email);
    }

    // MEMBER MANAGEMENT

    @Transactional
    public WorkspaceMember addMember(Long workspaceId, String currentUserEmail, String newMemberEmail, WorkspaceMember.WorkspaceRole role) {
        Workspace workspace = getWorkspaceById(workspaceId, currentUserEmail);

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User newMember = userRepository.findByEmail(newMemberEmail)
                .orElseThrow(() -> new RuntimeException("Member to add not found"));

        //  Seul l'ADMIN peut ajouter des membres
        checkWorkspaceAdmin(workspace, currentUser);

        // Vérifie qu'il n'est pas déjà membre
        if (workspaceMemberRepository.findByWorkspaceAndUser(workspace, newMember).isPresent()) {
            throw new RuntimeException("User is already a member of this workspace");
        }

        WorkspaceMember member = WorkspaceMember.builder()
                .workspace(workspace)
                .user(newMember)
                .role(role)
                .build();

        log.info("Member {} added to workspace {} with role {}", newMemberEmail, workspaceId, role);
        return workspaceMemberRepository.save(member);
    }

    @Transactional
    public void removeMember(Long workspaceId, String currentUserEmail, String memberEmailToRemove) {
        Workspace workspace = getWorkspaceById(workspaceId, currentUserEmail);

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User memberToRemove = userRepository.findByEmail(memberEmailToRemove)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        //  Seul l'ADMIN peut supprimer des membres
        checkWorkspaceAdmin(workspace, currentUser);

        workspaceMemberRepository.deleteByWorkspaceAndUser(workspace, memberToRemove);
        log.info("Member {} removed from workspace {}", memberEmailToRemove, workspaceId);
    }

    // PERMISSION CHECKS

    public void checkWorkspaceMembership(Workspace workspace, User user) {
        boolean isMember = workspaceRepository.isMember(workspace.getId(), user);
        if (!isMember) {
            throw new RuntimeException("You are not a member of this workspace");
        }
    }

    public void checkWorkspaceAdmin(Workspace workspace, User user) {
        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceAndUser(workspace, user)
                .orElseThrow(() -> new RuntimeException("You are not a member of this workspace"));

        if (member.getRole() != WorkspaceMember.WorkspaceRole.ADMIN) {
            throw new RuntimeException("You don't have admin permission in this workspace");
        }
    }

    public WorkspaceMember.WorkspaceRole getUserWorkspaceRole(Long workspaceId, String email) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return workspaceMemberRepository.findByWorkspaceAndUser(workspace, user)
                .orElseThrow(() -> new RuntimeException("User is not a member of this workspace"))
                .getRole();
    }
}