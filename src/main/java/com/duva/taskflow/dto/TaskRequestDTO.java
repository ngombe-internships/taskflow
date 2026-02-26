package com.duva.taskflow.dto;

import com.duva.taskflow.entity.enums.Priority;
import com.duva.taskflow.entity.enums.Status;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskRequestDTO {
//le TaskRequestDTO est utiliser pour cree et update
    @NotBlank
    private String title;

    private String description;

    private LocalDate startDate;

    private LocalDate dueDate;

    private Priority priority;

    private Status status;
}