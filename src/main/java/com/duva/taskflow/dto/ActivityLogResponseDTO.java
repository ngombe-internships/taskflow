package com.duva.taskflow.dto;

import com.duva.taskflow.entity.ActivityLog;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ActivityLogResponseDTO - Réponse pour un log d'activité
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLogResponseDTO {

    private Long id;
    private ActivityLog.ActionType actionType;
    private String description;
    private String fieldChanged;
    private String oldValue;
    private String newValue;
    private String user;  // Email
    private LocalDateTime createdAt;
}