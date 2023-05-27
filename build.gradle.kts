plugins {
    java
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("net.researchgate.release") version "3.0.2"
    `maven-publish`
    signing
}

group = "io.github.pillisan42"
version = "1.0.2-SNAPSHOT"
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

tasks.create<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.getByName("javadoc"))
}

tasks.create<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main)
}


publishing {
    repositories {
        maven {
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.findProperty("ossrhUsername") as String
                password = project.findProperty("ossrhPassword") as String
            }
        }
    }
    publications {
        create<MavenPublication>("jfx-borderless-native") {
            groupId = "io.github.pillisan42"
            artifactId = "jfx-borderless-native"
            version = project.findProperty("version") as String
            from(components["java"])
        }
        withType<MavenPublication> {
            pom {
                name.set("JFxBorderlessNative")
                description.set("This library provide true support of Windows 10 Aero Snap On JavaFx" +
                        " Undecorated non transparent Borderless window Native code is based on example " +
                        "found on this repository https://github.com/melak47/BorderlessWindow")
                url.set("git@github.com:pillisan42/JFxBorderlessNative.git")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("pillisan42")
                        name.set("pillisan42")
                        email.set("pillisan42@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git@github.com:pillisan42/JFxBorderlessNative.git")
                    developerConnection.set("scm:https://github.com/pillisan42/JFxBorderlessNative.git")
                    url.set("https://github.com/pillisan42/JFxBorderlessNative")
                }
            }
        }
    }
    configure<SigningExtension> {
        sign(publishing.publications["jfx-borderless-native"])
    }
}

release {
    //versionProperties.set(listOf(version))
    //tagTemplate.set("JFxBorderlessNative_${version}")
    buildTasks.set(listOf("build",/*"javadocJar","sourcesJar",*/"publish"))
    versionPropertyFile.set("gradle.properties")
}