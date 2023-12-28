import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("io.ktor.plugin") version "2.3.7"
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
}

group = "dev.vlaship"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

val logbackVersion = "1.4.14"

dependencies {
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-netty")

    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-server-request-validation")

    implementation("io.ktor:ktor-server-double-receive")
    implementation("io.ktor:ktor-server-call-logging")
    implementation("io.ktor:ktor-server-default-headers")
    implementation("io.ktor:ktor-server-compression")
//	implementation("io.ktor:ktor-server-locations")
    implementation("io.ktor:ktor-server-call-id")
    implementation("io.ktor:ktor-server-rate-limit")
    implementation("io.ktor:ktor-server-status-pages")
    implementation("io.ktor:ktor-server-auto-head-response")

    implementation("ch.qos.logback:logback-classic:$logbackVersion")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "21"
    }
}

application {
    mainClass.set("dev.vlaship.App")
}