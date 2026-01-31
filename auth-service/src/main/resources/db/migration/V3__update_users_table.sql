-- Add new columns
ALTER TABLE core.users
ADD COLUMN designation VARCHAR(100);

ALTER TABLE core.users
ADD COLUMN mobile VARCHAR(15) UNIQUE;

ALTER TABLE core.users
ADD COLUMN is_verified BOOLEAN DEFAULT FALSE;

-- Modify email length (PostgreSQL allows this safely)
ALTER TABLE core.users
ALTER COLUMN email TYPE VARCHAR(150);

-- Remove password column (OTP-based auth)
ALTER TABLE core.users
DROP COLUMN password;
