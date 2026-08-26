package io.kommunicate.services

import io.kommunicate.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class KmClientServiceTest {

    @Test
    fun regionalApiHostsMapToValidDashboardAndHelpCenterUrls() {
        val arrays = readUrlArrays()
        val apiUrls = arrays.getValue("km_base_url")
        val dashboardUrls = arrays.getValue("km_dashboard_url")
        val helpCenterUrls = arrays.getValue("km_helpcenter_url")

        assertEquals(apiUrls.size, dashboardUrls.size)
        assertEquals(apiUrls.size, helpCenterUrls.size)

        listOf(BuildConfig.EU_API_SERVER_URL, "https://api-in.kommunicate.io").forEach { apiUrl ->
            val index = apiUrls.indexOf(apiUrl)
            assertTrue("Missing regional API mapping for $apiUrl", index >= 0)
            assertEquals("https://dashboard.kommunicate.io", dashboardUrls[index])
            assertEquals("https://helpcenter.kommunicate.io", helpCenterUrls[index])
        }
    }

    private fun readUrlArrays(): Map<String, List<String>> {
        val workingDirectory = System.getProperty("user.dir")
            ?: error("Unable to determine working directory")
        val root = generateSequence(File(workingDirectory).canonicalFile) {
            it.parentFile
        }.firstOrNull { File(it, "settings.gradle").isFile }
            ?: error("Unable to locate repository root")
        val source = File(root, "kommunicate/src/main/res/values/km_urls.xml")

        val factory = DocumentBuilderFactory.newInstance().apply {
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            setFeature("http://xml.org/sax/features/external-general-entities", false)
            setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            isXIncludeAware = false
            isExpandEntityReferences = false
        }
        val arrays = factory.newDocumentBuilder().parse(source).getElementsByTagName("string-array")

        return (0 until arrays.length).associate { index ->
            val array = arrays.item(index) as Element
            val items = array.getElementsByTagName("item")
            array.getAttribute("name") to (0 until items.length).map { itemIndex ->
                items.item(itemIndex).textContent.trim()
            }
        }
    }
}
