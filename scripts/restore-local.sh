#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
if mysql --protocol=TCP -h127.0.0.1 -P3307 -uroot -N -e "SELECT SCHEMA_NAME FROM information_schema.SCHEMATA WHERE SCHEMA_NAME='LibraryWeb2026'" | rg -q LibraryWeb2026; then
  echo 'LibraryWeb2026 already exists; refusing to overwrite existing data.' >&2
  exit 1
fi
python3 scripts/restore_backup.py "${1:-../LibraryDB}" > .runtime/restore.sql
mysql --protocol=TCP -h127.0.0.1 -P3307 -uroot < .runtime/restore.sql
mysql --protocol=TCP -h127.0.0.1 -P3307 -uroot LibraryWeb2026 < scripts/migration.sql
