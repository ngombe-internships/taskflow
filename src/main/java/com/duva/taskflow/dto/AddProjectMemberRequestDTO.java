package com.duva.taskflow.dto;

import com.duva.taskflow.entity.ProjectMember;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * AddProjectMemberRequestDTO - Requête pour ajouter un membre à un projet
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddProjectMemberRequestDTO {

    @NotBlank(message = "Email is required")
    private String email;

    private ProjectMember.ProjectRole role;
}