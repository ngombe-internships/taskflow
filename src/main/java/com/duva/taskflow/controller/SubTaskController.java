package com.duva.taskflow.controller;

import com.duva.taskflow.dto.SubTaskRequestDTO;
import com.duva.taskflow.dto.SubTaskResponseDTO;
import com.duva.taskflow.service.SubTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subtasks")
@RequiredArgsConstructor
public class SubTaskController {

    private final SubTaskService subTaskService;

    // Create subtask under a task
    @PostMapping("/task/{taskId}")
    public SubTaskResponseDTO createSubTask(@PathVariable Long taskId,
                                            @RequestBody SubTaskRequestDTO dto) {
        return subTaskService.createSubTask(taskId, dto);
    }

    // Get paginated subtasks of a task
    @GetMapping("/task/{taskId}")
    public Page<SubTaskResponseDTO> getSubTasks(@PathVariable Long taskId,
                                                Pageable pageable) {
        return subTaskService.getSubTasks(taskId, pageable);
    }

    // Update subtask
    @PutMapping("/{subTaskId}")
    public SubTaskResponseDTO updateSubTask(@PathVariable Long subTaskId,
                                            @RequestBody SubTaskRequestDTO dto) {
        return subTaskService.updateSubTask(subTaskId, dto);
    }

    // Delete subtask
    @DeleteMapping("/{subTaskId}")
    public String deleteSubTask(@PathVariable Long subTaskId) {
        subTaskService.deleteSubTask(subTaskId);
        return "SubTask deleted successfully";
    }
}