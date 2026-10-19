-- Original backup is restored first. Tombstones retain loan foreign keys when removing a copy.
ALTER TABLE bookcopy ADD COLUMN deletedAt DATETIME NULL DEFAULT NULL;
