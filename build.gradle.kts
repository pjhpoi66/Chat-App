plugins {
    kotlin("jvm") version "2.2.0"
    application
}

group = "com.joongho"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("com.joongho.chat.ChatServerKt")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}