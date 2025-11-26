ALTER TABLE portal_portal_app ADD COLUMN IF NOT EXISTS is_installed BOOLEAN;

UPDATE portal_portal_app SET is_installed = TRUE WHERE installed = 'yes';

ALTER TABLE portal_portal_app DROP COLUMN IF EXISTS installed;