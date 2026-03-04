package com.duva.taskflow.repository;

import com.duva.taskflow.entity.Project;
import com.duva.taskflow.entity.Workspace;
import com.duva.taskflow.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    // Récupère les projets d'un workspace dont l'utilisateur est membre
    @Query("SELECT p FROM Project p " +
            "JOIN p.members pm " +
            "WHERE p.workspace = :workspace AND pm.user = :user")
    Page<Project> findUserProjectsInWorkspace(@Param("workspace") Workspace workspace,
                                              @Param("user") User user, Pageable pageable);

    // Vérifie si un utilisateur est membre d'un projet
    @Query("SELECT CASE WHEN COUNT(pm) > 0 THEN true ELSE false END " +
            "FROM ProjectMember pm " +
            "WHERE pm.project.id = :projectId AND pm.user = :user")
    boolean isMember(@Param("projectId") Long projectId, @Param("user") User user);
}