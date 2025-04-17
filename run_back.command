#!/bin/bash

cd "$(dirname "$0")/backend/db-pcr-backend"

export PATH="$HOME/.sdkman/candidates/maven/current/bin:$PATH"
export PATH="$HOME/.sdkman/candidates/java/current/bin:$PATH"

mvn spring-boot:run

echo ""
echo "Press any key to exit..."
read -n 1
