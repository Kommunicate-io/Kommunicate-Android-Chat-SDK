package io.kommunicate.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class SSLPinningPinInventoryTest {

    @Test
    fun kotlinAndReleaseXmlPinInventoriesMatch() {
        val kotlinInventory = SSLPinningConfig.pinInventory()

        releaseNetworkSecurityConfigs().forEach { config ->
            val xmlInventory = parsePinInventory(config)
            assertEquals("Host or pin mismatch in ${config.path}", kotlinInventory, xmlInventory)
            assertTrue(
                "Every host must have distinct primary and backup pins in ${config.path}",
                xmlInventory.values.all { pins -> pins.size == 2 }
            )
        }
    }

    private fun releaseNetworkSecurityConfigs(): List<File> {
        val workingDirectory = System.getProperty("user.dir")
            ?: error("Unable to determine working directory")
        val root = generateSequence(File(workingDirectory).canonicalFile) {
            it.parentFile
        }.firstOrNull { File(it, "settings.gradle").isFile }
            ?: error("Unable to locate repository root")

        return listOf("app", "kommunicate", "kommunicateui").map { module ->
            File(root, "$module/src/release/res/xml/network_security_config.xml").also {
                check(it.isFile) { "Missing release network security config: ${it.path}" }
            }
        }
    }

    private fun parsePinInventory(config: File): Map<String, Set<String>> {
        val factory = DocumentBuilderFactory.newInstance().apply {
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            setFeature("http://xml.org/sax/features/external-general-entities", false)
            setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            isXIncludeAware = false
            isExpandEntityReferences = false
        }
        val document = factory.newDocumentBuilder().parse(config)
        val domainConfigs = document.getElementsByTagName("domain-config")

        return (0 until domainConfigs.length).associate { index ->
            val domainConfig = domainConfigs.item(index) as Element
            val domains = domainConfig.getElementsByTagName("domain")
            check(domains.length == 1) { "Expected one exact domain in ${config.path}" }
            val host = domains.item(0).textContent.trim()
            val pinNodes = domainConfig.getElementsByTagName("pin")
            val pins = (0 until pinNodes.length)
                .map { pinIndex -> pinNodes.item(pinIndex).textContent.trim() }
                .toSet()
            host to pins
        }
    }
}
