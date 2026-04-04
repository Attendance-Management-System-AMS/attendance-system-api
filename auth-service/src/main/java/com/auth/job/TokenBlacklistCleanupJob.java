package com.auth.job;

import com.auth.repository.TokenBlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class TokenBlacklistCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistCleanupJob.class);

    private final TokenBlacklistRepository tokenBlacklistRepository;

    // Xóa các token đã hết hạn khỏi blacklist theo lịch chạy định kỳ.
    @Scheduled(fixedDelayString = "${auth.blacklist.cleanup-interval-ms:3600000}")
    @Transactional
    public void cleanupExpiredTokens() {
        int deleted = tokenBlacklistRepository.deleteExpiredTokens(OffsetDateTime.now());
        if (deleted > 0) {
            log.info("Da xoa {} token het han khoi blacklist", deleted);
        }
    }
}
