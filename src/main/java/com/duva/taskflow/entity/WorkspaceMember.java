package com.duva.taskflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * WorkspaceMember - Représente l'appartenance d'un utilisateur à un workspace
 *
 * Chaque utilisateur a un rôle dans le workspace (ADMIN, MEMBER, VIEWER)
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "workspace_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"workspace_id", "user_id"})
})
public class WorkspaceMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkspaceRole role; // ADMIN, MEMBER, VIEWER

    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
    }

    // Rôles dans un Workspace

    public enum WorkspaceRole {
        ADMIN,   // Peut tout faire
        MEMBER,  // Peut créer/modifier/supprimer ses propres projets et tâches
        VIEWER   // Peut seulement consulter
    }
}