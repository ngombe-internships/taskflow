package com.duva.taskflow.dto;

import com.duva.taskflow.entity.enums.Status;
import lombok.Data;

@Data
public class SubTaskRequestDTO {

    private String title;

    private String description;

    private Status status;
}