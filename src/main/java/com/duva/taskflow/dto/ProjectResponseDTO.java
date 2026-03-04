package com.duva.taskflow.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * ProjectResponseDTO - Réponse pour un projet
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponseDTO {

    private Long id;
    private String name;
    private String description;
    private Long workspaceId;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}