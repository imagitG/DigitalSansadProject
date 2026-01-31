-- =========================
-- OTP VERIFICATION TABLE
-- =========================
CREATE TABLE core.otp_verification (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    otp_hash TEXT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user
        FOREIGN KEY (user_id)
        REFERENCES core.users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_otp_user_expiry
ON core.otp_verification(user_id, expires_at);
