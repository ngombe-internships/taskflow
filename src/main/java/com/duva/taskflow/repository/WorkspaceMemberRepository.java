package com.duva.taskflow.repository;

import com.duva.taskflow.entity.WorkspaceMember;
import com.duva.taskflow.entity.Workspace;
import com.duva.taskflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {

    Optional<WorkspaceMember> findByWorkspaceAndUser(Workspace workspace, User user);

    void deleteByWorkspaceAndUser(Workspace workspace, User user);
}