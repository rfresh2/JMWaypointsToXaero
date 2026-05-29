plugins {
    java
    id("com.gradleup.shadow") version "8.3.7"
}

group = "com.github.rfresh2"
version = "1.2"

val shade: Configuration by configurations.creating
configurations.implementation.get().extendsFrom(shade)

java {
    toolchain { languageVersion = JavaLanguageVersion.of(8) }
}

repositories {
    mavenCentral()
    maven("https://repo.viaversion.com")
}

dependencies {
    shade("com.fasterxml.jackson.core:jackson-core:2.21.3")
    shade("com.fasterxml.jackson.core:jackson-databind:2.21.3")
    shade("org.slf4j:slf4j-api:2.0.18")
    shade("org.slf4j:slf4j-simple:2.0.18")
    shade("com.viaversion:nbt:5.2.1")
    shade("com.formdev:flatlaf:3.7.1")
}

tasks {
    withType(JavaCompile::class.java) {
        options.encoding = "UTF-8"
        options.isDeprecation = true
    }
    jar { enabled = false }
    shadowJar {
        archiveBaseName.set(project.name)
        archiveClassifier.set("")
        archiveVersion.set(project.version.toString())

        configurations = listOf(shade)

        manifest {
            attributes(mapOf(
                "Implementation-Title" to "JMWaypointsToXaero",
                "Implementation-Version" to project.version.toString(),
                "Main-Class" to "com.github.rfresh2.JourneyMapWaypointsToXaero"
            ))
        }
    }
    build {
        dependsOn("shadowJar")
    }
}
