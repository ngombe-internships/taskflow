package com.duva.taskflow.dto;

import com.duva.taskflow.entity.WorkspaceMember;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * AddMemberRequestDTO - Requête pour ajouter un membre à un workspace
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddMemberRequestDTO {

    @NotBlank(message = "Email is required")
    private String email;

    private WorkspaceMember.WorkspaceRole role;
}