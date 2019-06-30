import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URL

plugins {
    kotlin("jvm") version "1.3.31"
    jacoco
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.4"
    id("org.jlleitschuh.gradle.ktlint") version "8.1.0"
    id("org.jlleitschuh.gradle.ktlint-idea") version "8.1.0"
    id("ru.vyarus.mkdocs") version "1.1.0"
    id("org.jetbrains.dokka") version "0.9.18"
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

val dokka by tasks.getting(DokkaTask::class) {
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"

    externalDocumentationLink {
        url = URL("https://docs.oracle.com/javase/8/docs/api/")
    }
}

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

mkdocs {
    strict = true

    python.pip("mkdocs-alabaster:0.8.0", "markdown-include:0.5.1")
}
