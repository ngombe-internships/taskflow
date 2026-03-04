package com.duva.taskflow.service;

import com.duva.taskflow.dto.CommentRequestDTO;
import com.duva.taskflow.dto.CommentResponseDTO;
import com.duva.taskflow.entity.*;
import com.duva.taskflow.repository.CommentRepository;
import com.duva.taskflow.repository.TaskRepository;
import com.duva.taskflow.repository.UserRepository;
import com.duva.taskflow.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;

/**
 * CommentService - Logique métier des commentaires
 *
 * Responsabilités:
 * - Créer/lire/mettre à jour/supprimer les commentaires
 * - Vérifier les permissions du projet
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    // CREATE

    @Transactional
    public CommentResponseDTO createComment(Long projectId, Long taskId, String email, CommentRequestDTO dto) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        //  Vérifier que la tâche appartient au projet
        if (!task.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Task does not belong to this project");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //  Vérifier que l'utilisateur est membre du projet
        projectMemberRepository.findByProjectAndUser(task.getProject(), user)
                .orElseThrow(() -> new RuntimeException("You are not a member of this project"));

        Comment comment = Comment.builder()
                .content(dto.getContent())
                .task(task)
                .createdBy(user)
                .build();

        Comment savedComment = commentRepository.save(comment);
        log.info("Comment created: {} on task: {} by user: {}", savedComment.getId(), taskId, email);

        return mapToDTO(savedComment);
    }

    // READ

    public Page<CommentResponseDTO> getTaskComments(Long projectId, Long taskId, String email, Pageable pageable) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        //  Vérifier que la tâche appartient au projet
        if (!task.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Task does not belong to this project");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //  Vérifier que l'utilisateur est membre du projet
        projectMemberRepository.findByProjectAndUser(task.getProject(), user)
                .orElseThrow(() -> new RuntimeException("You are not a member of this project"));

        Page<Comment> comments = commentRepository.findByTask(task, pageable);
        log.debug("Found {} comments for task: {}", comments.getTotalElements(), taskId);

        return comments.map(this::mapToDTO);
    }

    public CommentResponseDTO getCommentById(Long projectId, Long taskId, Long commentId, String email) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        //  Vérifier que la tâche appartient au projet
        if (!task.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Task does not belong to this project");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //  Vérifier que l'utilisateur est membre du projet
        projectMemberRepository.findByProjectAndUser(task.getProject(), user)
                .orElseThrow(() -> new RuntimeException("You are not a member of this project"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        //  Vérifier que le commentaire appartient à la tâche
        if (!comment.getTask().getId().equals(taskId)) {
            throw new RuntimeException("Comment does not belong to this task");
        }

        return mapToDTO(comment);
    }

    // UPDATE

    @Transactional
    public CommentResponseDTO updateComment(Long projectId, Long taskId, Long commentId, String email, CommentRequestDTO dto) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Task does not belong to this project");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        //  Seul le créateur du commentaire peut le modifier
        if (!comment.getCreatedBy().getId().equals(user.getId())) {
            throw new RuntimeException("You can only edit your own comments");
        }

        if (!comment.getTask().getId().equals(taskId)) {
            throw new RuntimeException("Comment does not belong to this task");
        }

        comment.setContent(dto.getContent());
        comment.setUpdatedAt(LocalDateTime.now());

        Comment updatedComment = commentRepository.save(comment);
        log.info("Comment {} updated by user: {}", commentId, email);

        return mapToDTO(updatedComment);
    }

    // DELETE

    @Transactional
    public void deleteComment(Long projectId, Long taskId, Long commentId, String email) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Task does not belong to this project");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        //  Seul le créateur ou un ADMIN du projet peut supprimer
        boolean isCreator = comment.getCreatedBy().getId().equals(user.getId());
        boolean isAdmin = projectMemberRepository.findByProjectAndUser(task.getProject(), user)
                .map(pm -> pm.getRole() == ProjectMember.ProjectRole.ADMIN)
                .orElse(false);

        if (!isCreator && !isAdmin) {
            throw new RuntimeException("You can only delete your own comments or be a project admin");
        }

        if (!comment.getTask().getId().equals(taskId)) {
            throw new RuntimeException("Comment does not belong to this task");
        }

        commentRepository.deleteById(commentId);
        log.info("Comment {} deleted by user: {}", commentId, email);
    }

    // HELPER

    private CommentResponseDTO mapToDTO(Comment comment) {
        return CommentResponseDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdBy(comment.getCreatedBy().getEmail())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}