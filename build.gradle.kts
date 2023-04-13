plugins {
    java
    id("org.openjfx.javafxplugin") version "0.0.13"
}

group = "fr.pilli"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

javafx {
    version = "19"
    modules = listOf("javafx.controls")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}