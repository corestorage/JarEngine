# JarEngine

![logo](https://github.com/user-attachments/assets/46e2219d-278a-4eda-a6c2-81411a8abfcd)


**JarEngine** is a modern, modular, and open-source Java ME (J2ME) emulator, rebranded and improved from the classic MicroEmulator project. It enables you to run MIDlet (J2ME) applications and games on your desktop, with a focus on a clean Swing-based UI, extensibility, and developer-friendliness.

---

## 🚀 Features

- **J2ME (MIDlet) Emulation**: Run classic Java ME apps and games on your desktop.
- **Modern Swing UI**: Clean, resizable, and user-friendly interface.
- **Proxy Support**: HTTP and SOCKS proxy configuration for networked MIDlets.
- **UpTimer**: Track elapsed time since MIDlet load.
- **Recording & Screenshots**: Capture animated GIFs and screenshots of your apps.
- **Device Skins & Scaling**: Emulate different device screens and resolutions.
- **Extensible Architecture**: Modular codebase for easy extension and device/plugin support.
- **Cross-Platform**: Runs on any OS with Java 11+ (Linux, Windows, macOS).

---

## 🏗️ Project Structure

```
JarEngine/
├── jarengine-cldc/         # CLDC core emulation
├── jarengine-midp/         # MIDP API emulation
├── jarengine-javase/       # Java SE backend (core logic)
├── jarengine-javase-swing/ # Swing-based desktop frontend
├── microemu-extensions/    # Optional device skins, JSRs, etc.
├── microemu-tests/         # Test MIDlets and utilities
├── build/                  # Build output (fat JARs, etc.)
├── README.md               # This file
└── ...
```

---

## 🛠️ Build & Run

### Prerequisites
- Java 11 or newer (OpenJDK or Oracle JDK)
- Maven 3.6+

### Build All Modules
```sh
mvn clean install
```

### Run the Emulator
After building, run the fat JAR from the `build/` directory:
```sh
cd build
java -jar JarEngine-1.0.0.jar
```

### Load a MIDlet
- Use **File → Open MIDlet File...** to load a `.jar` or `.jad` file.
- Use **File → Open MIDlet URL...** to load from a URL.

---

## 💡 Contributing

Contributions are welcome! To get started:
1. Fork the repo and create a feature branch.
2. Make your changes (keep code modular and clean).
3. Submit a pull request with a clear description.

Please see the code comments and module structure for guidance.

---

## 📄 License

JarEngine is dual-licensed under the **LGPL 2.1** and **Apache License 2.0**. See the `LICENSE`, `COPYING`, and `COPYING-AL-2.0` files for details.

---

## 🙏 Credits

- Based on the original [MicroEmulator](https://github.com/barteo/microemu) by Bartek Teodorczyk and contributors.
- Modernized, refactored, and maintained as **JarEngine** by the open-source community.

---

## 📫 Contact

For questions, suggestions, or support, open an issue or start a discussion on the project repository. 
