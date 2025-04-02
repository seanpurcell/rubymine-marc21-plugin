plugins {
    id("org.jetbrains.intellij") version "1.17.3"
    kotlin("jvm") version "1.9.22"
}

group = "com.example"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.marc4j:marc4j:2.8.1")
}

intellij {
    version.set("2024.3.5")
    type.set("IU")
    plugins.set(listOf("com.intellij.java"))
}

tasks {
    patchPluginXml {
        changeNotes.set("Adds a custom editor tab for .mrc files using marc4j.")
    }
}
