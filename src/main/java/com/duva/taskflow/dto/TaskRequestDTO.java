package com.duva.taskflow.dto;

import com.duva.taskflow.entity.enums.Priority;
import com.duva.taskflow.entity.enums.Status;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;

/**
 * TaskRequestDTO - Requête pour créer/modifier une tâche
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskRequestDTO {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private Status status;
    private Priority priority;
    private LocalDate startDate;
    private LocalDate dueDate;

    // Optional: email de la personne à qui assigner
    private String assignedToEmail;
}