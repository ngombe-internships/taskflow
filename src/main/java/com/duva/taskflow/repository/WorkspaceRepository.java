package com.duva.taskflow.repository;

import com.duva.taskflow.entity.Workspace;
import com.duva.taskflow.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    // Récupère les workspaces dont l'utilisateur est membre
    @Query("SELECT w FROM Workspace w " +
            "JOIN w.members wm " +
            "WHERE wm.user = :user")
    Page<Workspace> findUserWorkspaces(@Param("user") User user, Pageable pageable);

    // Vérifie si un utilisateur est membre d'un workspace
    @Query("SELECT CASE WHEN COUNT(wm) > 0 THEN true ELSE false END " +
            "FROM WorkspaceMember wm " +
            "WHERE wm.workspace.id = :workspaceId AND wm.user = :user")
    boolean isMember(@Param("workspaceId") Long workspaceId, @Param("user") User user);
}