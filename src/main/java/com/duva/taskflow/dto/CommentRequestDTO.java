package com.duva.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * CommentRequestDTO - Requête pour créer/modifier un commentaire
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentRequestDTO {

    @NotBlank(message = "Comment content is required")
    private String content;
}

