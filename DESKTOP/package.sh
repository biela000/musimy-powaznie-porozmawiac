#!/usr/bin/env bash
# Build a native executable for the desktop app using jpackage.
# Run from the DESKTOP directory:  bash package.sh
#
# Output:
#   macOS  →  target/dist/MPP.app  (drag to Applications) or  target/dist/MPP-1.0.dmg
#   Windows→  target/dist/MPP-1.0.exe  (installer)
#   Linux  →  target/dist/mpp-1.0.deb  or  .rpm
#
# Requirements: JDK 21+, Maven, messenger module already installed (mvn install in messenger/)

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo "==> Building messenger module..."
(cd ../messenger && mvn install -q -DskipTests)

echo "==> Building fat JAR..."
mvn package -q -DskipTests

FAT_JAR="target/desktop-fat.jar"
if [ ! -f "$FAT_JAR" ]; then
    echo "ERROR: fat JAR not found at $FAT_JAR"
    exit 1
fi

echo "==> Running jpackage..."
rm -rf target/dist
jpackage \
    --type app-image \
    --input target \
    --main-jar desktop-fat.jar \
    --main-class com.jtjmpm.desktop.Launcher \
    --name "MPP" \
    --app-version "1.0" \
    --dest target/dist \
    --java-options "--add-opens javafx.graphics/com.sun.javafx.application=ALL-UNNAMED" \
    --java-options "-Xmx512m"

echo ""
echo "==> Done! Executable is in: target/dist/"
ls target/dist/
