package com.duva.taskflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ProjectMember - Représente l'appartenance d'un utilisateur à un projet
 *
 * Chaque utilisateur a un rôle dans le projet (ADMIN, MEMBER, VIEWER)
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "project_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"project_id", "user_id"})
})
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectRole role; // ADMIN, MEMBER, VIEWER

    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
    }

    // Rôles dans un Project

    public enum ProjectRole {
        ADMIN,   // Peut tout faire dans le projet
        MEMBER,  // Peut créer/modifier tâches, changer statut
        VIEWER   // Peut seulement consulter
    }
}