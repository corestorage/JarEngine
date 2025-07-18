# MicroEmulator (JarEngine)

MicroEmulator is a pure Java implementation of J2ME (MIDP/CLDC) for J2SE. It allows you to run and test Java ME MIDlet applications as standalone Java desktop apps or as applets.

## Features
- Emulates Java ME (MIDP/CLDC) apps on desktop
- Swing-based GUI for easy interaction
- Supports device skins, extensions, and custom devices
- Modular structure for easy extension

## Requirements
- Java 8+ (JDK 1.8 or higher)
- Maven 3.x

## Building
```sh
mvn clean package -Dmaven.test.skip
```
- Output JARs will be in the `target/` directories of each module.

## Running
To run the main emulator GUI:
```sh
java -jar microemulator/target/microemulator-3.0.0-SNAPSHOT.jar
```

## Loading MIDlets
- Use the GUI to load `.jad` or `.jar` files.
- You can also run MIDlets from the command line:
  ```sh
  java -jar microemulator/target/microemulator-3.0.0-SNAPSHOT.jar path/to/your.midlet.jar
  ```

## Modules
- `microemu-javase` — Core desktop emulator logic
- `microemu-javase-swing` — Swing GUI frontend
- `microemu-midp` — MIDP/CLDC logic and default launcher
- `microemu-android`, `microemu-iphone` — Platform ports
- `microemu-extensions` — Device profiles, JSRs, and more
- `microemu-examples` — Example MIDlets

## Development
- IDE: You can import as a Maven project in IntelliJ IDEA, Eclipse, or VS Code.
- To generate Eclipse project files:
  ```sh
  mvn eclipse:eclipse -DdownloadSources=true
  ```

## Contributing
Pull requests and issues are welcome! Please:
- Fork the repo
- Create a feature branch
- Submit a pull request with a clear description

## License
This project is dual-licensed under the LGPL 2.1+ and Apache License 2.0. See the `COPYING` and `COPYING-AL-2.0` files for details.

## Credits
Originally by Bartek Teodorczyk and contributors.

---

For more details, see the legacy `README` file or the documentation in each module. 