package com.duva.taskflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ActivityLog - Représente une action effectuée sur une tâche
 *
 * Track:
 * - Création de tâche
 * - Modifications de tâche
 * - Changement de statut
 * - Assignation
 * - Etc.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "activity_logs", indexes = {
        @Index(name = "idx_task_id", columnList = "task_id"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tâche affectée
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    // Utilisateur qui a fait l'action
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Type d'action
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType actionType;

    // Description lisible
    @Column(columnDefinition = "TEXT")
    private String description;

    // Ancienne valeur (pour les changements)
    @Column(columnDefinition = "TEXT")
    private String oldValue;

    // Nouvelle valeur (pour les changements)
    @Column(columnDefinition = "TEXT")
    private String newValue;

    // Champ qui a changé (pour les modifications)
    private String fieldChanged;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Types d'actions

    public enum ActionType {
        TASK_CREATED,        // Tâche créée
        TASK_UPDATED,        // Tâche modifiée
        STATUS_CHANGED,      // Statut changé
        PRIORITY_CHANGED,    // Priorité changée
        ASSIGNED,            // Assignée à quelqu'un
        UNASSIGNED,          // Désassignée
        COMMENT_ADDED,       // Commentaire ajouté
        COMMENT_DELETED,     // Commentaire supprimé
        DUE_DATE_CHANGED,    // Date limite changée
        DESCRIPTION_CHANGED, // Description changée
        TASK_DELETED         // Tâche supprimée
    }
}