package com.attendance.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "token_blacklist", indexes = {
        @Index(name = "idx_token_blacklist_user_id", columnList = "user_id"),
        @Index(name = "idx_token_blacklist_expires_at", columnList = "expires_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token_jti", nullable = false, unique = true)
    private String tokenJti;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    // JTI của refresh token mới thay thế token này (dùng cho grace period).
    @Column(name = "replaced_by_jti")
    private String replacedByJti;

    // Thời điểm token bị blacklist (dùng để tính grace period).
    @Column(name = "blacklisted_at")
    private OffsetDateTime blacklistedAt;

    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}




