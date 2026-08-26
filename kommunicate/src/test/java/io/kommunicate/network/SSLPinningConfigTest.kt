package io.kommunicate.network

import android.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PublicKey
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

@RunWith(RobolectricTestRunner::class)
class SSLPinningConfigTest {

    @Test
    fun pinsOnlyKommunicateControlledHosts() {
        assertTrue(SSLPinningConfig.isPinnedHost("api.kommunicate.io"))
        assertTrue(SSLPinningConfig.isPinnedHost("CHAT-EU.KOMMUNICATE.IO."))
        assertFalse(SSLPinningConfig.isPinnedHost("api-ca.kommunicate.io"))
        assertTrue(SSLPinningConfig.isRetiredHost("API-CA.KOMMUNICATE.IO."))
        assertFalse(SSLPinningConfig.isPinnedHost("unknown.kommunicate.io"))
        assertFalse(SSLPinningConfig.isPinnedHost("kommunicate.io.attacker.example"))
        assertFalse(SSLPinningConfig.isPinnedHost("storage.googleapis.com"))
        assertFalse(SSLPinningConfig.isPinnedHost("s3.amazonaws.com"))
    }

    @Test
    fun primaryAndBackupPinsAreDistinct() {
        val primary = "TVBTCkZ55/FSdN5KDEWeF6aQMEsf4tmuHbQy92W4OuY="
        val backup = "OdSlmQD9NWJh4EbcOHBxkhygPwNSwA9Q91eounfbcoE="

        assertTrue(SSLPinningConfig.hasPrimaryPin("api.kommunicate.io", primary))
        assertFalse(SSLPinningConfig.hasBackupPin("api.kommunicate.io", primary))
        assertTrue(SSLPinningConfig.hasBackupPin("api.kommunicate.io", backup))
        assertFalse(SSLPinningConfig.hasPrimaryPin("api.kommunicate.io", backup))
        assertFalse(SSLPinningConfig.hasPrimaryPin("api-test.kommunicate.io", primary))
    }

    @Test
    fun createsSha256SpkiPinFromPublicKey() {
        val publicKey = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }
            .generateKeyPair().public
        val expected = Base64.encodeToString(
            MessageDigest.getInstance("SHA-256").digest(publicKey.encoded),
            Base64.NO_WRAP
        )

        assertEquals(expected, SSLPinningConfig.publicKeyPin(publicKey))
    }

    @Test
    fun verifierAcceptsPrimaryPin() {
        val fixture = verifierFixture()

        assertTrue(fixture.verifier.verify(fixture.hostname, sessionWith(fixture.primaryKey)))
    }

    @Test
    fun verifierAcceptsBackupPin() {
        val fixture = verifierFixture()

        assertTrue(fixture.verifier.verify(fixture.hostname, sessionWith(fixture.backupKey)))
    }

    @Test
    fun verifierRejectsUnknownPin() {
        val fixture = verifierFixture()
        val unknownKey = generatePublicKey()

        assertFalse(fixture.verifier.verify(fixture.hostname, sessionWith(unknownKey)))
    }

    @Test
    fun verifierUsesPlatformValidationForNonKommunicateHost() {
        val fixture = verifierFixture()
        val unknownKey = generatePublicKey()

        assertTrue(fixture.verifier.verify("storage.googleapis.com", sessionWith(unknownKey)))

        val rejectingPlatformVerifier = HostnameVerifier { _, _ -> false }
        val verifier = SSLPinningConfig.createHostnameVerifier(
            rejectingPlatformVerifier,
            fixture.hostPins,
            emptySet()
        )
        assertFalse(verifier.verify("storage.googleapis.com", sessionWith(unknownKey)))
    }

    private fun verifierFixture(): VerifierFixture {
        val hostname = "api.kommunicate.io"
        val primaryKey = generatePublicKey()
        val backupKey = generatePublicKey()
        val hostPins = mapOf(
            hostname to SSLPinningConfig.PinSet(
                SSLPinningConfig.publicKeyPin(primaryKey),
                SSLPinningConfig.publicKeyPin(backupKey)
            )
        )
        val verifier = SSLPinningConfig.createHostnameVerifier(
            HostnameVerifier { _, _ -> true },
            hostPins,
            emptySet()
        )
        return VerifierFixture(hostname, primaryKey, backupKey, hostPins, verifier)
    }

    private fun sessionWith(publicKey: PublicKey): SSLSession {
        val certificate = mock(X509Certificate::class.java)
        `when`(certificate.publicKey).thenReturn(publicKey)
        val session = mock(SSLSession::class.java)
        `when`(session.peerCertificates).thenReturn(arrayOf(certificate))
        return session
    }

    private fun generatePublicKey(): PublicKey =
        KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }
            .generateKeyPair().public

    private data class VerifierFixture(
        val hostname: String,
        val primaryKey: PublicKey,
        val backupKey: PublicKey,
        val hostPins: Map<String, SSLPinningConfig.PinSet>,
        val verifier: HostnameVerifier
    )
}
