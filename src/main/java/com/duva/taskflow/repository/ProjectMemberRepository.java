package com.duva.taskflow.repository;

import com.duva.taskflow.entity.ProjectMember;
import com.duva.taskflow.entity.Project;
import com.duva.taskflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    Optional<ProjectMember> findByProjectAndUser(Project project, User user);

    void deleteByProjectAndUser(Project project, User user);
}