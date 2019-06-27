import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.31"
    jacoco
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.4"
    id("org.jlleitschuh.gradle.ktlint") version "8.1.0"
    id("org.jlleitschuh.gradle.ktlint-idea") version "8.1.0"
}

group = "com.hypercubetools"
version = "0.1-alpha"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = Versions.targetJvm
}

testWithJunit()
coverageWithJacoco()

bintray {
    user = System.getenv("BINTRAY_USER") ?: project.properties["bintray.user"]?.toString()
    key = System.getenv("BINTRAY_KEY")
    publish = true

    setPublications("lib")

    with(pkg) {
        repo = project.group.toString()
        name = project.name
        setLicenses("MIT")
        with(version) {
            name = project.version.toString()
        }
    }
}

publishing {
    publications {
        register<MavenPublication>("lib") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            pom.withXml {
                asNode().apply {
                    appendNode("name", project.name)
                    appendNode("licenses").appendNode("license").apply {
                        appendNode("name", "MIT")
                        appendNode("url", "https://opensource.org/licenses/MIT")
                        appendNode("distribution", "repo")
                    }
                }
            }
        }
    }
}
