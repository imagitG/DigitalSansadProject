-- V4__add_user_role.sql

ALTER TABLE core.users
ADD COLUMN role VARCHAR(50);

-- optional: set default for existing users
UPDATE core.users
SET role = 'USER'
WHERE role IS NULL;

-- enforce NOT NULL after backfill
ALTER TABLE core.users
ALTER COLUMN role SET NOT NULL;
