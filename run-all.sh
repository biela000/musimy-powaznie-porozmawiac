#!/bin/bash

REPO_ROOT="$(cd "$(dirname "$0")" && pwd)"
API_DIR="$REPO_ROOT/API"
DESKTOP_DIR="$REPO_ROOT/DESKTOP"

echo ">>> Building messenger module..."
cd "$REPO_ROOT/messenger" && mvn install -q

echo ">>> Starting API..."
cd "$API_DIR"
mvn spring-boot:run &
API_PID=$!

echo ">>> Waiting for API to be ready..."
until curl -s http://localhost:3000 > /dev/null 2>&1 || \
      curl -s http://localhost:3000/actuator/health > /dev/null 2>&1; do
  sleep 1
done
echo ">>> API is up (PID $API_PID)"

echo ">>> Starting Desktop instance 1 (port 8080)..."
cd "$DESKTOP_DIR"
mvn javafx:run -Djavafx.args="8080" &
D1_PID=$!

echo ">>> Starting Desktop instance 2 (port 8081)..."
mvn javafx:run -Djavafx.args="8081" &
D2_PID=$!

echo ">>> All processes started. PIDs: API=$API_PID, D1=$D1_PID, D2=$D2_PID"

trap "kill $API_PID $D1_PID $D2_PID 2>/dev/null" EXIT

# Wait for both desktop instances — kill API when both exit
wait $D1_PID $D2_PID
echo ">>> Desktop instances closed. Stopping API..."
kill $API_PID
