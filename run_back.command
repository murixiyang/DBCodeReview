#!/bin/bash

cd "$(dirname "$0")/backend/db-pcr-backend"

# 2) Ensure the DB container is running...
CONTAINER_NAME=code-review-db
DB_NAME=mydb
DB_USER=user
DB_PASS=password

if [ -n "$RUNNING_ID" ]; then
  echo "✔ Postgres container is already running (ID=$RUNNING_ID)."
elif [ -n "$ALL_ID" ]; then
  echo "▶ Starting existing Postgres container (ID=$ALL_ID)..."
  docker start "$ALL_ID"
else
  echo "✚ Creating & starting Postgres container..."

  docker run -d \
    --name "$CONTAINER_NAME" \
    -e POSTGRES_DB="$DB_NAME" \
    -e POSTGRES_USER="$DB_USER" \
    -e POSTGRES_PASSWORD="$DB_PASS" \
    -p 5432:5432 \
    -v code-review-db-data:/var/lib/postgresql/data \
    postgres:15

fi

# 3) Wait for Postgres to accept connections (authenticate as $DB_USER)
echo -n "Waiting for Postgres to be ready on localhost:5432"
until pg_isready -h localhost -p 5432 -U "$DB_USER" >/dev/null 2>&1; do
  echo -n "."
  sleep 1
done
echo " ✅ Postgres is up!"

# 4) Run your backend
export PATH="$HOME/.sdkman/candidates/maven/current/bin:$PATH"
export PATH="$HOME/.sdkman/candidates/java/current/bin:$PATH"

mvn spring-boot:run

echo ""
echo "Press any key to exit..."
read -n 1