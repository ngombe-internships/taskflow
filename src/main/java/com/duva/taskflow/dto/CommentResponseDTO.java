package com.duva.taskflow.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * CommentResponseDTO - Réponse pour un commentaire
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponseDTO {

    private Long id;
    private String content;
    private String createdBy;      // Email de qui a créé
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
