DROP TABLE IF EXISTS portal_directory_entry_directory_entry_custom_fields;
DROP TABLE IF EXISTS portal_directory_entry_directory_contact_set;
DROP TABLE IF EXISTS portal_directory_entry_directory_entry_category_set;
DROP TABLE IF EXISTS portal_directory_entry;

DROP TABLE IF EXISTS portal_directory_category_directory_category_set;
DROP TABLE IF EXISTS portal_directory_category;

DROP SEQUENCE IF EXISTS portal_directory_category_seq;
DROP SEQUENCE IF EXISTS portal_directory_contact_seq;
DROP SEQUENCE IF EXISTS portal_directory_entry_seq;

DELETE FROM meta_field WHERE meta_model IN (Select id from meta_model where name IN ('DirectoryEntry','DirectoryCategory'));
DELETE FROM meta_model WHERE name IN ('DirectoryEntry','DirectoryCategory');

DELETE FROM meta_menu WHERE name IN ('menu-portal-directory-root','menu-portal-directory-entry','menu-portal-directory-category');
DELETE FROM meta_action WHERE name IN ('action-directory-entry-attrs-set-workspace','action.portal.directory.entry','action.portal.directory.category');
DELETE FROM meta_view WHERE name IN ('directory-entry-grid','directory-entry-form','directory-category-grid','directory-category-form');