-- Add foreign key constraint from sow_actions to sow_documents
ALTER TABLE core.sow_actions
ADD CONSTRAINT fk_sow_actions_sow_documents
FOREIGN KEY (sow_id) REFERENCES core.sow_documents(id) ON DELETE CASCADE;

-- Add NOT NULL constraint to sow_id if not already present
ALTER TABLE core.sow_actions
ALTER COLUMN sow_id SET NOT NULL;
