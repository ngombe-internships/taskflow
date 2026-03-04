package com.duva.taskflow.repository;

import com.duva.taskflow.entity.Task;
import com.duva.taskflow.entity.Project;
import com.duva.taskflow.entity.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // Récupère toutes les tâches d'un projet (paginé)
    Page<Task> findByProject(Project project, Pageable pageable);

    // Récupère les tâches d'un projet avec un statut spécifique (paginé)
    Page<Task> findByProjectAndStatus(Project project, Status status, Pageable pageable);
}