// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

import java.net.URI

plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(17)
}

// Since build-logic is special we have to cheat to test/get the alternativeResolutionRepo
val gradlePropertiesFile = file("../gradle.properties")
val alternativeResolutionRepo = if (gradlePropertiesFile.exists()) {
    gradlePropertiesFile.readLines().firstOrNull { line ->
        line.startsWith("alternativeResolutionRepo=")
    }?.substringAfter("alternativeResolutionRepo=")
} else {
    null
}

repositories {
    mavenCentral()
    google()  // gestalt uses an annotation package by Google
    gradlePluginPortal()

    maven {
        val repoViaEnv = System.getenv()["RESOLUTION_REPO"]
        if (alternativeResolutionRepo != null) {
            // If the user supplies an alternative repo via gradle.properties then use that
            name = "from alternativeResolutionRepo in gradle.properties"
            url =  URI(alternativeResolutionRepo)
        } else if (repoViaEnv != null && repoViaEnv != "") {
            name = "from \$RESOLUTION_REPO"
            url = URI(repoViaEnv)
        } else {
            // Our default is the main virtual repo containing everything except repos for testing Artifactory itself
            name = "Terasology Artifactory"
            url = URI("https://artifactory.terasology.io/artifactory/virtual-repo-live")
            //url = URI("https://artifactory.nanoware.us/artifactory/virtual-repo-live")
        }
    }

    // TODO MYSTERY: As of November 7th 2021 virtual-repo-live could no longer be relied on for latest snapshots - Pro feature?
    // We've been using it that way for *years* and nothing likewise changed in the area for years as well. This seems to work ....
    maven {
        name = "Terasology snapshot locals"
        url = URI("https://artifactory.terasology.io/artifactory/terasology-snapshot-local")
    }
}

dependencies {
    implementation("org.terasology:reflections:0.9.12-MB") {
        because("reflections-manifest.gradle.kts")
    }
    // Additional corrections for old reflections dependencies:
    constraints {
        implementation("com.google.guava:guava:31.1-jre")
        implementation("org.javassist:javassist:3.29.0-GA")
        implementation("net.bytebuddy:bytebuddy:1.14.8")
    }

    // graph analysis
    implementation("org.jgrapht:jgrapht-core:1.5.0")

    // for inspecting modules
    implementation("org.terasology.gestalt:gestalt-module:8.0.0-SNAPSHOT")

    // plugins we configure
    implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:5.2.3")
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:5.0.0.4638")

    api(kotlin("test"))
}

group = "org.terasology.gradology"
