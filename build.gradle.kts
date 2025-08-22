import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.24"
    application
    // JavaFX 플러그인 추가
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "com.joongho"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // 코루틴 라이브러리 추가
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    // TornadoFX 라이브러리 추가
    implementation("no.tornado:tornadofx:1.7.20")
    testImplementation(kotlin("test"))
}

// JavaFX 설정
javafx {
    version = "21" // JavaFX 버전
    modules = listOf("javafx.controls", "javafx.fxml") // 사용할 모듈
}

// 애플리케이션 실행 설정
application {
    // 기본 실행 클래스를 서버로 지정
    mainClass.set("com.joongho.chat.ChatServerKt")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

// GUI 클라이언트를 실행하기 위한 별도의 Gradle 태스크 생성
tasks.register<JavaExec>("runClient") {
    group = "application"
    description = "Runs the TornadoFX Chat Client"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.joongho.chat.ChatClientGUIKt")
}
