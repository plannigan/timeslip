import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.gradle.testing.jacoco.tasks.rules.JacocoViolationRule
import org.w3c.dom.Document
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.File
import java.io.StringReader
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

private const val minBranchCoverage = .75
private const val minInstructionCoverage = .8

fun Project.coverageWithJacoco() {
    val check by tasks
    val jacocoTestReport by tasks

    tasks.withType<JacocoCoverageVerification> {
        check.dependsOn(this)
        dependsOn(jacocoTestReport)

        violationRules {
            rule {
                element = "BUNDLE"
                excludes = listOf("com.jacoco.dto.*")
                limit("BRANCH", minBranchCoverage)
                limit("INSTRUCTION", minInstructionCoverage)
            }
        }
    }

    tasks.withType<JacocoReport> {
        reports {
            html.required.set(true)
            xml.required.set(true)
            csv.required.set(false)
        }

        // Jacoco doesn't provide an easy way to display the total coverage,
        // so parse it out of the XML report
        doLast {
            try {
                val report = loadXml("$buildDir/reports/jacoco/test/jacocoTestReport.xml")
                val attributes = report.counterAttributes("INSTRUCTION")
                val missed = attributes.getInt("missed")
                val covered = attributes.getInt("covered")
                val ratio: Double = if (missed == 0 && covered == 0) {
                    0.0
                } else {
                    covered.toDouble() / (covered + missed) * 100
                }
                println("Total code coverage: ${ratio.format(2)}%")
            } catch (ex: IllegalStateException) {
                logger.error("Could not display coverage total: ${ex.message}")
            }
        }
    }
}

private fun JacocoViolationRule.limit(type: String, minValue: Double) {
    this.limit {
        counter = type
        minimum = minValue.toBigDecimal()
    }
}

/**
 * Retrieves the [java][org.gradle.api.plugins.JavaPluginConvention] project convention.
 */
private val Project.java: org.gradle.api.plugins.JavaPluginConvention
    get() = convention.getPluginByName("java")


private fun loadXml(file: String): Document {
    val doc = documentBuilder().parse(File(file))
    doc.normalizeDocument()
    return doc
}

/**
 * Retrieve the attributes for the counter element.
 *
 * @throws IllegalStateException Document does not contain exactly one counter for given type.
 */
private fun Document.counterAttributes(type: String): NamedNodeMap {
    val counters = xPath().evaluate("/report/counter[@type='$type']", this, XPathConstants.NODESET) as NodeList
    if (counters.length == 1) {
        return counters.item(0).attributes
    } else {
        throw invalidDocument("$type counter")
    }
}

/**
 * Retrieve a value as an integer.
 *
 * @throws IllegalStateException The given key does not map to a value or the value is not an integer.
 */
private fun NamedNodeMap.getInt(name: String): Int {
    val value = getNamedItem(name) ?: throw invalidDocument("$name attribute")
    try {
        return value.nodeValue.toInt()
    } catch (ex: NumberFormatException) {
        throw IllegalStateException("$name attribute is not an integer ($value)")
    }
}

private fun documentBuilder(): DocumentBuilder {
    val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    builder.setEntityResolver { _, systemId ->
        if (systemId.endsWith(".dtd")) {
            InputSource(StringReader(""))
        } else {
            null
        }
    }
    return builder
}

private fun xPath() = XPathFactory.newInstance().newXPath()
private fun invalidDocument(missing: String) = IllegalStateException("Could not parse coverage report because $missing was not found.")
fun Double.format(digits: Int) : String = java.lang.String.format("%.${digits}f", this)
