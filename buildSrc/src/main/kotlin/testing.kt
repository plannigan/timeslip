import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat


fun Project.testWithJunit() {
    dependencies {
        "testImplementation"(Deps.hamkrest)
        "testImplementation"(Deps.hamcrest)
        "testImplementation"(Deps.junitApi)
        "testImplementation"(Deps.junitParams)
        "testRuntimeOnly"(Deps.junitEngine)
        "testRuntimeOnly"(Deps.kotlinReflection)
    }

    tasks.withType<Test> {
        useJUnitPlatform()

        testLogging {
            showExceptions = true
            showCauses = true
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
        }

        reports.html.required.set(false)
        reports.junitXml.required.set(true)
    }
}
