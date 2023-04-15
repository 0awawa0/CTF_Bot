
version = "2.0.4"
val javafxVersion = "17.0.2"
javafx { version = javafxVersion }

repositories {
    google()
    mavenCentral()
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
}

plugins {
    java
    kotlin("jvm") version "1.8.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.10"
    id("org.openjfx.javafxplugin") version "0.0.9"
    id("org.jetbrains.compose") version "1.4.0"
}

dependencies {
    val telegramBotsVersion = "6.5.0"
    val kotlinVersion = "1.8.10"
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
    implementation("org.xerial:sqlite-jdbc:3.41.2.1")

    // UI
    implementation("no.tornado:tornadofx:2.0.0-SNAPSHOT")
    implementation(compose.desktop.currentOs)
    implementation(compose.material)
    implementation(compose.materialIconsExtended)

    testImplementation("org.junit.jupiter:junit-jupiter:5.7.2")
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    manifest {
        attributes["Main-Class"] = "ui.ApplicationKt"
    }
    from (
        configurations.compileClasspath.get().map {
            if (it.isDirectory) it else zipTree(it)
        }
    )
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).configureEach {
    kotlinOptions {
        jvmTarget = "11"
    }
}

