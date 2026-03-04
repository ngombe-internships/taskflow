package com.duva.taskflow.repository;

import com.duva.taskflow.entity.ActivityLog;
import com.duva.taskflow.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    // Récupère l'historique d'une tâche (paginé)
    Page<ActivityLog> findByTask(Task task, Pageable pageable);

    // Récupère l'historique par type d'action
    Page<ActivityLog> findByTaskAndActionType(Task task, ActivityLog.ActionType actionType, Pageable pageable);

    // Compte les actions sur une tâche
    long countByTask(Task task);
}