package com.duva.taskflow.repository;

import com.duva.taskflow.entity.Task;
import com.duva.taskflow.entity.Project;
import com.duva.taskflow.entity.enums.Status;
import com.duva.taskflow.entity.enums.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

/**
 * TaskSearchRepository - Requêtes de recherche avancée pour les tâches
 *
 * Permet:
 * - Recherche par titre/description
 * - Filtrer par statut
 * - Filtrer par priorité
 * - Filtrer par dates
 * - Filtrer par assigné
 */
public interface TaskSearchRepository extends JpaRepository<Task, Long> {

    // SEARCH BY TEXT

    /**
     * Recherche par titre ou description (LIKE)
     */
    @Query("SELECT t FROM Task t WHERE t.project = :project AND " +
            "(LOWER(t.title) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchText, '%')))")
    Page<Task> searchByTitleOrDescription(@Param("project") Project project,
                                          @Param("searchText") String searchText,
                                          Pageable pageable);

    // FILTER BY STATUS

    /**
     * Recherche par titre ET statut
     */
    @Query("SELECT t FROM Task t WHERE t.project = :project AND t.status = :status AND " +
            "LOWER(t.title) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    Page<Task> searchByTitleAndStatus(@Param("project") Project project,
                                      @Param("searchText") String searchText,
                                      @Param("status") Status status,
                                      Pageable pageable);

    /**
     * Filtrer par statut uniquement
     */
    @Query("SELECT t FROM Task t WHERE t.project = :project AND t.status = :status")
    Page<Task> findByProjectAndStatus(@Param("project") Project project,
                                      @Param("status") Status status,
                                      Pageable pageable);

    // FILTER BY PRIORITY

    /**
     * Recherche par titre ET priorité
     */
    @Query("SELECT t FROM Task t WHERE t.project = :project AND t.priority = :priority AND " +
            "LOWER(t.title) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    Page<Task> searchByTitleAndPriority(@Param("project") Project project,
                                        @Param("searchText") String searchText,
                                        @Param("priority") Priority priority,
                                        Pageable pageable);

    /**
     * Filtrer par priorité uniquement
     */
    @Query("SELECT t FROM Task t WHERE t.project = :project AND t.priority = :priority")
    Page<Task> findByProjectAndPriority(@Param("project") Project project,
                                        @Param("priority") Priority priority,
                                        Pageable pageable);

    // FILTER BY DUE DATE

    /**
     * Recherche les tâches avec date limite avant une certaine date
     */
    @Query("SELECT t FROM Task t WHERE t.project = :project AND t.dueDate <= :dueDate " +
            "ORDER BY t.dueDate ASC")
    Page<Task> findTasksDueBeforeDate(@Param("project") Project project,
                                      @Param("dueDate") LocalDate dueDate,
                                      Pageable pageable);

    /**
     * Recherche les tâches avec date limite après une certaine date
     */
    @Query("SELECT t FROM Task t WHERE t.project = :project AND t.dueDate >= :dueDate " +
            "ORDER BY t.dueDate ASC")
    Page<Task> findTasksDueAfterDate(@Param("project") Project project,
                                     @Param("dueDate") LocalDate dueDate,
                                     Pageable pageable);

    // FILTER BY ASSIGNED USER

    /**
     * Recherche les tâches assignées à un utilisateur
     */
    @Query("SELECT t FROM Task t WHERE t.project = :project AND t.assignedTo.email = :email")
    Page<Task> findTasksAssignedTo(@Param("project") Project project,
                                   @Param("email") String email,
                                   Pageable pageable);

    /**
     * Recherche les tâches non assignées
     */
    @Query("SELECT t FROM Task t WHERE t.project = :project AND t.assignedTo IS NULL")
    Page<Task> findUnassignedTasks(@Param("project") Project project, Pageable pageable);

    // ADVANCED FILTER (COMBINING MULTIPLE FILTERS)

    /**
     * Recherche avancée: texte + statut + priorité
     */
    @Query("SELECT t FROM Task t WHERE t.project = :project AND " +
            "LOWER(t.title) LIKE LOWER(CONCAT('%', :searchText, '%')) AND " +
            "t.status = :status AND t.priority = :priority")
    Page<Task> advancedSearch(@Param("project") Project project,
                              @Param("searchText") String searchText,
                              @Param("status") Status status,
                              @Param("priority") Priority priority,
                              Pageable pageable);

    /**
     * Recherche: texte + statut + assigné à
     */
    @Query("SELECT t FROM Task t WHERE t.project = :project AND " +
            "LOWER(t.title) LIKE LOWER(CONCAT('%', :searchText, '%')) AND " +
            "t.status = :status AND t.assignedTo.email = :assignedToEmail")
    Page<Task> searchByTextStatusAndAssignee(@Param("project") Project project,
                                             @Param("searchText") String searchText,
                                             @Param("status") Status status,
                                             @Param("assignedToEmail") String assignedToEmail,
                                             Pageable pageable);
}