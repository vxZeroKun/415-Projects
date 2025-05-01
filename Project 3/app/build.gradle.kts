plugins {
    application
}

repositories {
    mavenCentral()
}

val lwjglVersion = "3.3.1"

dependencies {
    // Utility library
    implementation("com.google.guava:guava:31.1-jre")
    
    // JOML for matrix math
    implementation("org.joml:joml:1.10.5")

    // LWJGL BOM and core modules
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
    implementation("org.lwjgl:lwjgl")
    implementation("org.lwjgl:lwjgl-glfw")
    implementation("org.lwjgl:lwjgl-opengl")
    implementation("org.lwjgl:lwjgl-stb")

    // Native runtime libraries for Windows x64
    //runtimeOnly("org.lwjgl:lwjgl::natives-windows")
    //runtimeOnly("org.lwjgl:lwjgl-glfw::natives-windows")
    //runtimeOnly("org.lwjgl:lwjgl-opengl::natives-windows")
    //runtimeOnly("org.lwjgl:lwjgl-stb::natives-windows")
    // Windows x64 only
    //runtimeOnly("org.lwjgl:lwjgl::natives-windows")
    //runtimeOnly("org.lwjgl:lwjgl-glfw::natives-windows")
    //runtimeOnly("org.lwjgl:lwjgl-opengl::natives-windows")
    //runtimeOnly("org.lwjgl:lwjgl-stb::natives-windows")
    // macOS Intel & Apple Silicon
    runtimeOnly("org.lwjgl:lwjgl::natives-macos")
    runtimeOnly("org.lwjgl:lwjgl::natives-macos-arm64")
    runtimeOnly("org.lwjgl:lwjgl-glfw::natives-macos")
    runtimeOnly("org.lwjgl:lwjgl-glfw::natives-macos-arm64")
    runtimeOnly("org.lwjgl:lwjgl-opengl::natives-macos")
    runtimeOnly("org.lwjgl:lwjgl-opengl::natives-macos-arm64")
    runtimeOnly("org.lwjgl:lwjgl-stb::natives-macos")
    runtimeOnly("org.lwjgl:lwjgl-stb::natives-macos-arm64")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass.set("com.makkigame.Main")
    // For Mac
    applicationDefaultJvmArgs = listOf("-XstartOnFirstThread")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
