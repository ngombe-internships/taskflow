package com.duva.taskflow.dto;

import com.duva.taskflow.entity.enums.Status;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SubTaskResponseDTO {

    private Long id;
    private String title;
    private String description;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
