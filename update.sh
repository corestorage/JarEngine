#!/bin/bash
set -e

# 1. Read version from version.txt
VERSION=$(cat version.txt | tr -d ' \n')

if [[ -z "$VERSION" ]]; then
  echo "[ERROR] version.txt is empty or missing!"
  exit 1
fi

# 2. Remove all cached parent POMs for org.microemu/JarEngine-parent (full cleanup)
echo "[INFO] Removing all cached parent POMs for org.microemu/JarEngine-parent from local Maven repo..."
rm -rf ~/.m2/repository/org/microemu/JarEngine-parent

# 3. Update root pom.xml <version> FIRST
echo "[INFO] Updating root pom.xml to version $VERSION..."
sed -i "0,/<version>.*<\\/version>/s//<version>$VERSION<\\/version>/" pom.xml

# 4. Update all module parent versions
echo "[INFO] Updating all module parent versions to $VERSION..."
mvn versions:update-parent -DparentVersion=$VERSION -DgenerateBackupPoms=false || true

# 5. (Optional) Update all module <version> tags if you use them
echo "[INFO] Updating all module <version> tags to $VERSION (if present)..."
mvn versions:set -DnewVersion=$VERSION -DgenerateBackupPoms=false || true

# 6. Double-check and forcibly rewrite all module POMs' <parent><version> to match
for pom in jarengine-*/pom.xml; do
  if [[ -f "$pom" ]]; then
    echo "[INFO] Forcibly setting <parent><version> in $pom to $VERSION..."
    sed -i "/<parent>/,/<\/parent>/s/<version>.*<\/version>/<version>$VERSION<\/version>/" "$pom"
  fi
done

# 7. Wait for file system flush
sleep 1

# 8. Clean Maven build state
echo "[INFO] Running 'mvn clean' to clear Maven state..."
mvn clean

# 9. Build with new version
echo "[INFO] Building project with version $VERSION..."
mvn clean package -Dmaven.test.skip

# 10. Print success message
echo "[SUCCESS] All POMs updated and project built with version $VERSION. Check the build/ directory for new JARs."
