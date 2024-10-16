package kommunicate.io.sample.utils

import kommunicate.io.sample.network.KommunicateDashboardAPI
import kotlinx.coroutines.runBlocking

object KmTestHelper {

    fun getBotIdsFromDashboard(autToken: String, dashboardAPI: KommunicateDashboardAPI): List<String> = runBlocking {
        val dashboardBotDetails = dashboardAPI
            .getBotDetails(autToken)
            .get("data")
            .asJsonArray

        dashboardBotDetails.map {
            it.asJsonObject.get("userName").asString
        }
    }
}