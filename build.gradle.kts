import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.dokka.gradle.DokkaTask
import ru.vyarus.gradle.plugin.mkdocs.task.MkdocsTask
import java.net.URL

plugins {
    alias(libs.plugins.kotlin.jvm)
    jacoco
    `maven-publish`
    alias(libs.plugins.ktlint)
    alias(libs.plugins.ktlint.idea)
    alias(libs.plugins.mkdocs)
    alias(libs.plugins.dokka)
    alias(libs.plugins.jacocolog)
}

group = "com.hypercubetools"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(libs.bundles.junit)
    testImplementation(libs.hamkrest)
    testImplementation(libs.hamcrest)
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.kotlin.refelction)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = libs.versions.targetJvm.get()
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        showExceptions = true
        showCauses = true
        showStackTraces = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }

    reports.html.required.set(false)
    reports.junitXml.required.set(true)
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)

    violationRules {
        rule {
            element = "BUNDLE"
            excludes = listOf("com.jacoco.dto.*")
            limit {
                counter = "BRANCH"
                minimum = BigDecimal(".75")
            }
            limit {
                counter = "INSTRUCTION"
                minimum = BigDecimal(".8")
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        html.required.set(true)
        xml.required.set(false)
        csv.required.set(false)
    }
}

fun DokkaTask.dokkaConfig(format: String, outputDir: String) {
    outputFormat = format
    outputDirectory = outputDir
    externalDocumentationLink {
        url = URL("https://docs.oracle.com/javase/8/docs/api/")
    }
}

// Generate the API documentation as html for javadoc distribution
val dokka by tasks.getting(DokkaTask::class) {
    dokkaConfig("html", "$buildDir/javadoc")
}

// Generate the API documentation as markdown for docs site
val dokkaMd by tasks.register<DokkaTask>("dokkaMd") {
    dokkaConfig("gfm", "src/doc/docs/api")
}

val packageJavadoc by tasks.registering(Jar::class) {
    dependsOn("dokka")
    archiveClassifier.set("javadoc")
    from(sourceSets.main.get().allSource)
}

publishing {
    publications {
        register<MavenPublication>("lib") {
            from(components["java"])

            artifact(tasks.kotlinSourcesJar.get())
            artifact(packageJavadoc.get())

            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            pom {
                name.set(project.name)
                description.set("Time manipulation for JVM test cases")
                url.set("https://timeslip.hypercubetools.com")

                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                        distribution.set("repo")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/plannigan/timeslip")
                    developerConnection.set("scm:git:git://github.com/plannigan/timeslip")
                    url.set("https://timeslip.hypercubetools.com")
                }
            }
        }
    }
}

mkdocs {
    strict = true

    python.pip("mkdocs-material:4.4.0", "markdown-include:0.5.1", "markdown-fenced-code-tabs:1.0.5")
}

fun MkdocsTask.dokkaDepends() {
    dependsOn("dokkaMd")
}

val mkdocsServe by tasks.getting(MkdocsTask::class) { dokkaDepends() }
val mkdocsBuild by tasks.getting(MkdocsTask::class) { dokkaDepends() }
