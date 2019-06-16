import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.31"
    jacoco
    id("org.jlleitschuh.gradle.ktlint") version "8.1.0"
    id("org.jlleitschuh.gradle.ktlint-idea") version "8.1.0"
}

group = "com.hypercubetools"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = Versions.targetJvm
}

testWithJunit()
coverageWithJacoco()