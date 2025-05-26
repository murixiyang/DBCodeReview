#!/usr/bin/env bash
#
# clear-db.command
# Wipes out the 'mydb' Postgres database by dropping & recreating the public schema.

#———— Configuration ——————————————————————————————————————————————————————
DB_HOST=localhost
DB_PORT=5432
DB_NAME=mydb
DB_USER=user
DB_PASS=password

#———— Navigate to project (optional; only needed if you rely on .env files here) ————————
cd "$(dirname "$0")/backend/db-pcr-backend" || exit 1

#———— Clear screen & show what we’re doing ——————————————————————————————————
clear
echo "🚀  Clearing Postgres database '$DB_NAME' on $DB_HOST:$DB_PORT …"

#———— Export password for psql ————————————————————————————————————————
export PGPASSWORD="$DB_PASS"

#———— Drop & recreate the public schema ———————————————————————————————
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -v ON_ERROR_STOP=1 <<SQL
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO "$DB_USER";
SQL

#———— Check for success ————————————————————————————————————————————————
if [ $? -eq 0 ]; then
  echo "✅  Database '$DB_NAME' has been cleared."
else
  echo "❌  Failed to clear database—see errors above."
fi

echo
read -n 1 -p "Press any key to exit…"
