# JarEngine

## Build with Gradle

1. **Build all modules (skip tests):**
   ```sh
   ./gradlew build -x test
   ```

2. **Build and run tests:**
   ```sh
   ./gradlew build
   ```

3. **Create a runnable fat JAR for the Swing app:**
   ```sh
   ./gradlew :jarengine-javase-swing:shadowJar
   java -jar jarengine-javase-swing/build/libs/JarEngine-1.0.0-all.jar
   ```

- Only the `-all.jar` is runnable (includes all dependencies).
- All other JARs are libraries for use as dependencies.

## Project Structure
- Multi-module Gradle project
- No split packages
- Clean, industry-standard layout

## Contributing
- Use the Gradle Wrapper (`./gradlew`) for all builds.
- PRs and issues welcome! 
