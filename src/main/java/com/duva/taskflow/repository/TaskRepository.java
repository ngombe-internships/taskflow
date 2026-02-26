package com.duva.taskflow.repository;

import com.duva.taskflow.entity.Task;
import com.duva.taskflow.entity.User;
import com.duva.taskflow.entity.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // Get paginated tasks of a specific user
    Page<Task> findByUser(User user, Pageable pageable);

    // Get paginated tasks of a user filtered by status
    Page<Task> findByUserAndStatus(User user, Status status, Pageable pageable);
}