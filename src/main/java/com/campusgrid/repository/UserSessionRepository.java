package com.campusgrid.repository;

import com.campusgrid.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findByRefreshToken(String refreshToken);

    List<UserSession> findByUserIdAndIsRevokedFalse(Long userId);

    @Modifying
    @Query("UPDATE UserSession s SET s.isRevoked = true WHERE s.user.id = :userId")
    void revokeAllUserSessions(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}