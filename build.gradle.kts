import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.24"
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "21" // 혹은 사용하시는 자바 버전에 맞게 설정
    // 코틀린 컴파일러에도 인코딩 관련 옵션이 필요할 경우 추가할 수 있습니다.
}
group = "com.joongho"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("no.tornado:tornadofx:1.7.20")

    // Exposed (Kotlin SQL Framework) 라이브러리 추가
    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
    implementation("org.jetbrains.exposed:exposed-java-time:0.41.1")

    // H2 Database (In-memory DB) 라이브러리 추가
    implementation("com.h2database:h2:2.2.224")

    testImplementation(kotlin("test"))
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainClass.set("com.joongho.chat.ChatServerKt")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

tasks.register<JavaExec>("runClient") {
    group = "application"
    description = "Runs the TornadoFX Chat Client"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.joongho.chat.ChatClientGUIKt")
}
