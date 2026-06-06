package com.eduquest.repository;

import com.eduquest.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// ── User Repository ───────────────────────────────────────
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.collegeName = :college ORDER BY u.totalXp DESC")
    List<User> findTopByCollegeOrderByXp(@Param("college") String college, Pageable pageable);

    @Query("SELECT u FROM User u ORDER BY u.totalXp DESC")
    Page<User> findAllOrderByXpDesc(Pageable pageable);
}

// ── Quiz Repository ───────────────────────────────────────
@Repository
interface QuizRepository extends JpaRepository<Quiz, String> {
    Page<Quiz> findByIsActiveTrue(Pageable pageable);
    Page<Quiz> findBySubjectContainingIgnoreCase(String subject, Pageable pageable);
    Page<Quiz> findByDifficulty(Quiz.Difficulty difficulty, Pageable pageable);
    Page<Quiz> findBySubjectAndDifficulty(String subject, Quiz.Difficulty difficulty, Pageable pageable);

    @Query("SELECT DISTINCT q.subject FROM Quiz q WHERE q.isActive = true ORDER BY q.subject")
    List<String> findDistinctSubjects();
}

// ── QuizAttempt Repository ────────────────────────────────
@Repository
interface QuizAttemptRepository extends JpaRepository<QuizAttempt, String> {
    List<QuizAttempt> findByUserIdOrderByAttemptedAtDesc(String userId, Pageable pageable);
    long countByUserId(String userId);

    @Query("SELECT AVG(qa.score * 100.0 / qa.totalQuestions) FROM QuizAttempt qa WHERE qa.user.id = :userId")
    Double findAvgAccuracyByUserId(@Param("userId") String userId);

    @Query("SELECT COALESCE(SUM(qa.xpEarned), 0) FROM QuizAttempt qa WHERE qa.user.id = :userId AND qa.attemptedAt >= :since")
    Integer sumXpEarnedSince(@Param("userId") String userId, @Param("since") java.time.LocalDateTime since);

    @Query("SELECT q.subject, COUNT(qa), AVG(qa.score * 100.0 / qa.totalQuestions) " +
           "FROM QuizAttempt qa JOIN qa.quiz q WHERE qa.user.id = :userId GROUP BY q.subject")
    List<Object[]> findSubjectStatsForUser(@Param("userId") String userId);
}

// ── UserBadge Repository ──────────────────────────────────
@Repository
interface UserBadgeRepository extends JpaRepository<UserBadge, String> {
    List<UserBadge> findByUserIdOrderByEarnedAtDesc(String userId);

    @Query("SELECT ub.badgeName FROM UserBadge ub WHERE ub.user.id = :userId")
    List<String> findBadgeNamesByUserId(@Param("userId") String userId);
}
