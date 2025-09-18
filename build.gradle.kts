import org.jetbrains.kotlin.gradle.dsl.JvmTarget

version = "2.1.0"
val javafxVersion = "17.0.2"
val kotlinVersion = "1.9.10"
javafx { version = javafxVersion }

repositories {
    google()
    mavenCentral()
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
}

plugins {
    val kotlinVersion = "1.9.10"
    java
    kotlin("jvm") version kotlinVersion
    id("org.jetbrains.compose") version "1.6.2"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.10"
    id("org.openjfx.javafxplugin") version "0.0.9"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    val telegramBotsVersion = "6.9.7.1"
    val serializationJsonVersion = "1.5.0"
    val coroutinesVersion = "1.6.4"

    implementation("org.openjfx:javafx-base:$javafxVersion:win")
    implementation("org.openjfx:javafx-base:$javafxVersion:linux")
    implementation("org.openjfx:javafx-base:$javafxVersion:mac")
    implementation("org.openjfx:javafx-base:$javafxVersion:mac-aarch64")

    implementation("org.openjfx:javafx-graphics:$javafxVersion:win")
    implementation("org.openjfx:javafx-graphics:$javafxVersion:linux")
    implementation("org.openjfx:javafx-graphics:$javafxVersion:mac")
    implementation("org.openjfx:javafx-graphics:$javafxVersion:mac-aarch64")

    implementation("org.openjfx:javafx-controls:$javafxVersion:win")
    implementation("org.openjfx:javafx-controls:$javafxVersion:linux")
    implementation("org.openjfx:javafx-controls:$javafxVersion:mac")
    implementation("org.openjfx:javafx-controls:$javafxVersion:mac-aarch64")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationJsonVersion")
    annotationProcessor("org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion")
    implementation("org.telegram:telegrambots:$telegramBotsVersion")
    implementation("com.intellij:forms_rt:7.0.3")

    implementation("org.slf4j:slf4j-api:2.0.5")
    implementation("org.slf4j:slf4j-simple:2.0.5")

    // Database
    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
    implementation("org.xerial:sqlite-jdbc:3.41.2.2")

    // UI
//    implementation("no.tornado:tornadofx:2.0.0-SNAPSHOT")
//
    implementation("com.googlecode.blaisemath.tornado:tornadofx-fx18k16:2.0.1")
    implementation(compose.foundation)
    implementation(compose.ui)
    implementation(compose.desktop.macos_arm64)
    implementation(compose.desktop.macos_x64)
    implementation(compose.desktop.windows_x64)
    implementation(compose.desktop.linux_arm64)
    implementation(compose.desktop.linux_x64)
    implementation(compose.material)
    implementation(compose.materialIconsExtended)
    implementation("com.darkrockstudios:mpfilepicker:1.0.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = "ui.ApplicationKt"
    }
    from (
        configurations.compileClasspath.get().map {
            if (it.isDirectory) it else zipTree(it)
        },
        configurations.runtimeClasspath.get().map {
            if (it.isDirectory) it else zipTree(it)
        }
    )
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).configureEach {
    kotlinOptions {
        jvmTarget = "11"
    }
}

