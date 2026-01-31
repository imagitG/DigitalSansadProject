-- =====================================================
-- SEED ROLES & ADMIN USER
-- =====================================================

-- Ensure UUID generation
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =====================================================
-- 1. INSERT ROLES (keep DEFAULT_ROLE as-is)
-- =====================================================

INSERT INTO core.roles (id, name)
VALUES
  (gen_random_uuid(), 'ADMIN'),
  (gen_random_uuid(), 'SOW_APPROVER'),
  (gen_random_uuid(), 'SOW_CREATOR'),
  (gen_random_uuid(), 'SOW_REVIEWER')
ON CONFLICT (name) DO NOTHING;

-- =====================================================
-- 2. INSERT ADMIN USER (OTP-based auth, no password)
-- =====================================================

INSERT INTO core.users (
  id,
  name,
  email,
  designation,
  mobile,
  is_verified,
  created_at
)
SELECT
  gen_random_uuid(),
  'System Administrator',
  'admin@digitalsansad.com',
  'ADMIN',
  '9999999999',
  TRUE,
  CURRENT_TIMESTAMP
WHERE NOT EXISTS (
  SELECT 1
  FROM core.users
  WHERE email = 'admin@digitalsansad.com'
);

-- =====================================================
-- 3. MAP ADMIN USER TO ADMIN ROLE
-- =====================================================

INSERT INTO core.user_roles (user_id, role_id)
SELECT
  u.id,
  r.id
FROM core.users u
JOIN core.roles r
  ON r.name = 'ADMIN'
WHERE u.email = 'admin@digitalsansad.com'
ON CONFLICT DO NOTHING;
