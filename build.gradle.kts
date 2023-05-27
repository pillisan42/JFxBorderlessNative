plugins {
    java
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("net.researchgate.release") version "3.0.2"
}

group = "fr.pilli"


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

tasks.withType<JavaCompile> {
    options.release.set(8)
}

release {
    //tagTemplate.set("JFxBorderlessNative_${version}")
    versionPropertyFile.set("gradle.properties")
}