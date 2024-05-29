plugins {
    java
    id("io.github.goooler.shadow") version "8.1.7"
}

group = "com.github.rfresh2"
version = "1.1"

val shade: Configuration by configurations.creating
configurations.implementation.get().extendsFrom(shade)

java {
    toolchain { languageVersion = JavaLanguageVersion.of(8) }
}

repositories {
    mavenCentral()
}

dependencies {
    shade("com.fasterxml.jackson.core:jackson-core:2.17.1")
    shade("com.fasterxml.jackson.core:jackson-databind:2.17.1")
    shade("org.slf4j:slf4j-api:2.0.13")
    shade("org.slf4j:slf4j-simple:2.0.13")
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
