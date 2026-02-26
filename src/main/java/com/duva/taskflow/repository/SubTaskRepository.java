package com.duva.taskflow.repository;

import com.duva.taskflow.entity.SubTask;
import com.duva.taskflow.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubTaskRepository extends JpaRepository<SubTask, Long> {

    // Get all subtasks of a task (non-paginated)
    List<SubTask> findByTask(Task task);

    // Get paginated subtasks of a task
    Page<SubTask> findByTask(Task task, Pageable pageable);
}