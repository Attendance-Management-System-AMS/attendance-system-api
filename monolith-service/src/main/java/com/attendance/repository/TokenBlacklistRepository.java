package com.attendance.repository;

import com.attendance.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {

    // Kiểm tra token theo jti đã bị blacklist chưa.
    boolean existsByTokenJti(String tokenJti);

    // Tìm bản ghi blacklist theo JTI (để tra cứu token thay thế trong grace period).
    Optional<TokenBlacklist> findByTokenJti(String tokenJti);

    // Xóa các bản ghi blacklist đã hết hạn.
    @Modifying
    @Query("DELETE FROM TokenBlacklist t WHERE t.expiresAt < ?1")
    int deleteExpiredTokens(OffsetDateTime now);
}




