package com.duva.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * ProjectRequestDTO - Requête pour créer/modifier un projet
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;
}
