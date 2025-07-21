# JarEngine

## Running the Application

After building the project with Gradle, the runnable JAR is located at:

```
jarengine-javase-swing/build/libs/JarEngine-1.0.0-all.jar
```

Run it with:

```
java -jar jarengine-javase-swing/build/libs/JarEngine-1.0.0-all.jar
```

**Note:**
- The file `build/libs/JarEngine-1.0.0.jar` in the root directory is not runnable (it does not contain the application classes or a Main-Class manifest entry).
- The fat JAR (`-all.jar`) is created by the `shadowJar` task in the `jarengine-javase-swing` module and includes all dependencies and the correct manifest.

## Building

To build the project and create the runnable JAR:

```
./gradlew clean build shadowJar
```

## Development

- Main application entry point: `org.jarengine.app.Main` (in `jarengine-javase-swing`)
- For development, you can also use:

```
./gradlew :jarengine-javase-swing:run
```

## Project Structure
- Multi-module Gradle project
- No split packages
- Clean, industry-standard layout

## Contributing
- Use the Gradle Wrapper (`./gradlew`) for all builds.
- PRs and issues welcome! 
