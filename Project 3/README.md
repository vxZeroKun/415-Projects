# Project 3

> A Java-based voxel engine built with LWJGL and Gradle.

## Quick Start

1. **Clone the repository**
   ```bash
   git clone <your-repo-url> project3
   cd project3
   ```
2. **Configure native dependencies**

   - Open `app/build.gradle.kts`.
   - Uncomment the native runtime entries for your platform:

     ```kotlin
     // Windows x64:
     // runtimeOnly("org.lwjgl:lwjgl::natives-windows")
     // runtimeOnly("org.lwjgl:lwjgl-glfw::natives-windows")
     // …

     // macOS (Intel & Apple Silicon):
     runtimeOnly("org.lwjgl:lwjgl::natives-macos")
     runtimeOnly("org.lwjgl:lwjgl::natives-macos-arm64")
     // …
     ```

3. **Make the Gradle wrapper executable**
   ```bash
   chmod +x app/gradlew
   ```
4. **Run the application**
   ```bash
   cd app
   ./gradlew run
   ```

Enjoy exploring, placing, and breaking blocks in your customized voxel world!
