package com.duva.taskflow.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * WorkspaceResponseDTO - Réponse pour un workspace
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceResponseDTO {

    private Long id;
    private String name;
    private String description;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}