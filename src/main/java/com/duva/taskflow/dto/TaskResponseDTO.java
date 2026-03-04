package com.duva.taskflow.dto;

import com.duva.taskflow.entity.enums.Priority;
import com.duva.taskflow.entity.enums.Status;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * TaskResponseDTO - Réponse pour une tâche
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponseDTO {

    private Long id;
    private String title;
    private String description;
    private Status status;
    private Priority priority;
    private LocalDate startDate;
    private LocalDate dueDate;

    //  Nouveaux champs pour Trello-like
    private String createdBy;      // Email de qui a créé
    private String assignedTo;      // Email de qui c'est assigné (optional)

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}