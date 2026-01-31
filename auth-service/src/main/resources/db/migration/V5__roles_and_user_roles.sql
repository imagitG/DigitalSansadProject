CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================
-- 1. DROP role column from users
-- ============================

ALTER TABLE core.users
DROP COLUMN IF EXISTS role;


-- ============================
-- 2. CREATE roles table
-- ============================

CREATE TABLE IF NOT EXISTS core.roles (
  id UUID PRIMARY KEY,
  name VARCHAR(50) UNIQUE NOT NULL
);


-- ============================
-- 3. CREATE user_roles mapping
-- ============================

CREATE TABLE IF NOT EXISTS core.user_roles (
  user_id UUID NOT NULL REFERENCES core.users(id) ON DELETE CASCADE,
  role_id UUID NOT NULL REFERENCES core.roles(id) ON DELETE CASCADE,
  PRIMARY KEY (user_id, role_id)
);


-- ============================
-- 4. INSERT DEFAULT_ROLE
-- ============================

INSERT INTO core.roles (id, name)
VALUES (
  gen_random_uuid(),
  'DEFAULT_ROLE'
)
ON CONFLICT (name) DO NOTHING;
