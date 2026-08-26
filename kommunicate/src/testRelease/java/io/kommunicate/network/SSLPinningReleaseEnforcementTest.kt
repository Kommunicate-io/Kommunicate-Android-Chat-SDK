package io.kommunicate.network

import org.junit.Assert.assertTrue
import org.junit.Test

class SSLPinningReleaseEnforcementTest {

    @Test
    fun releaseBuildAlwaysEnforcesPinning() {
        assertTrue(SSLPinningConfig.isPinningEnabled())
    }
}
