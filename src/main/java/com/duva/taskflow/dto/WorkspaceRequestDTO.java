package com.duva.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * WorkspaceRequestDTO - Requête pour créer/modifier un workspace
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;
}
