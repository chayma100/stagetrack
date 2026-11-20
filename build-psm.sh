#!/usr/bin/env bash
# Construit psm.jar (Spring Boot + Angular) sous Linux / macOS / Git Bash.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"

echo "1/3 Build Angular..."
(cd "$ROOT/frontend" && npm install --legacy-peer-deps && npx ng build --configuration production)

echo "2/3 Copie d'Angular dans backend/src/main/resources/static..."
rm -rf "$ROOT/backend/src/main/resources/static"
mkdir -p "$ROOT/backend/src/main/resources/static"
cp -r "$ROOT/frontend/dist/." "$ROOT/backend/src/main/resources/static/"

echo "3/3 Build Spring Boot..."
(cd "$ROOT/backend" && mvn clean package -DskipTests)

echo "Terminé : backend/target/psm.jar"
echo "Lancer : java -jar backend/target/psm.jar   puis ouvrir http://localhost:8080"
