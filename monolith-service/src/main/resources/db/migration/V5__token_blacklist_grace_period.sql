-- Thêm cột hỗ trợ grace period cho refresh token rotation.
-- replaced_by_jti: JTI của refresh token mới đã thay thế token này.
-- blacklisted_at: Thời điểm token bị blacklist (dùng để tính grace period).

ALTER TABLE token_blacklist ADD COLUMN IF NOT EXISTS replaced_by_jti VARCHAR(255);
ALTER TABLE token_blacklist ADD COLUMN IF NOT EXISTS blacklisted_at TIMESTAMP WITH TIME ZONE DEFAULT NOW();

-- Index để tìm token thay thế nhanh hơn.
CREATE INDEX IF NOT EXISTS idx_token_blacklist_replaced_by_jti ON token_blacklist (replaced_by_jti);
