CREATE SCHEMA IF NOT EXISTS core;

CREATE TABLE IF NOT EXISTS core.sow_documents (
  id UUID PRIMARY KEY,
  title TEXT NOT NULL,
  ref_no TEXT,
  status TEXT NOT NULL,
  current_owner_role TEXT NOT NULL,
  created_by UUID NOT NULL,
  file_path TEXT NOT NULL,
  file_name TEXT,
  content_type TEXT,
  created_at TIMESTAMP,
  approved_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS core.sow_actions (
  id UUID PRIMARY KEY,
  sow_id UUID NOT NULL,
  action_type TEXT NOT NULL,
  acted_by UUID NOT NULL,
  from_role TEXT,
  to_role TEXT,
  comment TEXT,
  acted_at TIMESTAMP
);
