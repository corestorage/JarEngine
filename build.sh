#!/bin/bash
set -e

VERSION=$(cat version.txt | tr -d ' \n')
if [[ -z "$VERSION" ]]; then
  echo "[ERROR] version.txt is empty or missing!"
  exit 1
fi

POM_VERSION=$(grep -m 1 '<version>' pom.xml | sed -E 's|.*<version>([^<]+)</version>.*|\1|')

if [[ "$VERSION" != "$POM_VERSION" ]]; then
  echo "[INFO] Detected version change: version.txt=$VERSION, pom.xml=$POM_VERSION"
  echo "[INFO] Running full version update and build..."
  rm -rf ~/.m2/repository/org/microemu/JarEngine-parent
  sed -i "0,/<version>.*<\/version>/s|<version>.*</version>|<version>$VERSION</version>|" pom.xml
  mvn versions:update-parent -DparentVersion=$VERSION -DgenerateBackupPoms=false || true
  mvn versions:set -DnewVersion=$VERSION -DgenerateBackupPoms=false || true
  for pom in jarengine-*/pom.xml; do
    if [[ -f "$pom" ]]; then
      sed -i "/<parent>/,/<\/parent>/s|<version>.*</version>|<version>$VERSION</version>|" "$pom"
    fi
  done
  sleep 1
  mvn clean
  mvn clean package -Dmaven.test.skip || true
  mvn clean package -Dmaven.test.skip
else
  echo "[INFO] No version change detected (version.txt=$VERSION, pom.xml=$POM_VERSION). Running normal build..."
  mvn clean
  if ! mvn clean package -Dmaven.test.skip; then
    echo "[WARN] First build failed or produced no JARs, retrying..."
    mvn clean package -Dmaven.test.skip
  fi
fi

if [[ -d build ]]; then
  echo "[SUCCESS] Build complete. JARs in build/:"
  ls -lh build
  JAR_COUNT=$(ls build/*.jar 2>/dev/null | wc -l)
  if [[ $JAR_COUNT -eq 0 ]]; then
    echo "[WARN] No JARs found in build/. Retrying build one more time..."
    mvn clean package -Dmaven.test.skip
    ls -lh build
    JAR_COUNT2=$(ls build/*.jar 2>/dev/null | wc -l)
    if [[ $JAR_COUNT2 -eq 0 ]]; then
      echo "[ERROR] Still no JARs found in build/. Please check the build logs for errors."
      exit 1
    fi
  fi
else
  echo "[ERROR] Build directory not found!"
  exit 1
fi
