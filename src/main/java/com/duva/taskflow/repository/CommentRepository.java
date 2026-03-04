package com.duva.taskflow.repository;

import com.duva.taskflow.entity.Comment;
import com.duva.taskflow.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Récupère tous les commentaires d'une tâche (paginé)
    Page<Comment> findByTask(Task task, Pageable pageable);

    // Compte les commentaires d'une tâche
    long countByTask(Task task);
}