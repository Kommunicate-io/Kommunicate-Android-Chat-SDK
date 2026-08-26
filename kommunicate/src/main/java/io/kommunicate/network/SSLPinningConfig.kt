package io.kommunicate.network

import android.util.Base64
import io.kommunicate.BuildConfig
import io.kommunicate.utils.KmAppSettingPreferences
import java.security.MessageDigest
import java.security.PublicKey
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.HostnameVerifier

/** Configures public-key pinning without replacing Android's platform trust manager. */
object SSLPinningConfig {

    internal data class PinSet(val primary: String, val backup: String) {
        val all = setOf(primary, backup)
    }

    /* Verified 2026-08-26. See docs/SSL_PIN_ROTATION.md for certificate metadata. */
    private val pinsByHost = mapOf(
        "api.kommunicate.io" to PinSet(
            "TVBTCkZ55/FSdN5KDEWeF6aQMEsf4tmuHbQy92W4OuY=",
            "OdSlmQD9NWJh4EbcOHBxkhygPwNSwA9Q91eounfbcoE="
        ),
        "api-test.kommunicate.io" to PinSet(
            "gY8gSDmi6lg/wV1MbM3FzNMYrcX6EKseYfJg7QnA02A=",
            "OdSlmQD9NWJh4EbcOHBxkhygPwNSwA9Q91eounfbcoE="
        ),
        "api-eu.kommunicate.io" to PinSet(
            "cqN64LYMqSbYAlnj5gqix01wTHc4f+IFGOe68iV7LHY=",
            "LoMHBotttiDko50Gi13uXW71eIy7LAttI+rYT8wXF4w="
        ),
        "api-in.kommunicate.io" to PinSet(
            "1zJGg1EdjGEFdxqpA0bNZoXNomD9oXfxci/SmbWp7e0=",
            "brzvtCELCIZUo4sD/qPX0ccRtPsd3DY6RfmxpOU9oB4="
        ),
        "chat.kommunicate.io" to PinSet(
            "MpB2iHEUO7nmKSdp/YuWdkjSP9hp7Ax7P/9c9jeGQO4=",
            "OdSlmQD9NWJh4EbcOHBxkhygPwNSwA9Q91eounfbcoE="
        ),
        "chat-test.kommunicate.io" to PinSet(
            "eWPDImElk5nVbsn19Uz9VXDUVfPispeX5ZlINItM/7c=",
            "OdSlmQD9NWJh4EbcOHBxkhygPwNSwA9Q91eounfbcoE="
        ),
        "chat-eu.kommunicate.io" to PinSet(
            "w/h1ivN8L5msp4gPmxqFr2qqqb+dvxG+XGd8XDatx+s=",
            "OdSlmQD9NWJh4EbcOHBxkhygPwNSwA9Q91eounfbcoE="
        )
    )

    /* Retired legacy endpoints remain denied so old manual configurations fail closed. */
    private val retiredHosts = setOf("api-ca.kommunicate.io")

    /**
     * Platform CA/validity checks still run because the default socket factory is retained.
     * The wrapped hostname verifier first performs Android's hostname check and then pinning.
     */
    @JvmStatic
    fun configure(connection: HttpsURLConnection) {
        if (!isPinningEnabled()) return

        connection.hostnameVerifier = createHostnameVerifier(
            HttpsURLConnection.getDefaultHostnameVerifier()
        )
    }

    internal fun createHostnameVerifier(
        platformHostnameVerifier: HostnameVerifier,
        hostPins: Map<String, PinSet> = pinsByHost,
        blockedHosts: Set<String> = retiredHosts
    ): HostnameVerifier {
        return HostnameVerifier { hostname, session ->
            if (!platformHostnameVerifier.verify(hostname, session)) {
                return@HostnameVerifier false
            }
            val normalizedHost = normalizeHost(hostname)
            if (normalizedHost in blockedHosts) {
                return@HostnameVerifier false
            }
            val pins = hostPins[normalizedHost]
            if (pins == null) {
                return@HostnameVerifier true
            }

            try {
                session.peerCertificates
                    .filterIsInstance<X509Certificate>()
                    .any { certificate -> publicKeyPin(certificate) in pins.all }
            } catch (_: Exception) {
                false
            }
        }
    }

    @JvmStatic
    internal fun isPinnedHost(hostname: String): Boolean {
        return pinsForHost(hostname) != null
    }

    @JvmStatic
    internal fun isRetiredHost(hostname: String): Boolean =
        normalizeHost(hostname) in retiredHosts

    private fun pinsForHost(hostname: String): PinSet? =
        pinsByHost[normalizeHost(hostname)]

    private fun normalizeHost(hostname: String): String = hostname.lowercase().trimEnd('.')

    internal fun isPinningEnabled(): Boolean {
        return BuildConfig.SSL_PINNING_ENFORCED || KmAppSettingPreferences.isSSLPinningEnabled
    }

    @JvmStatic
    internal fun publicKeyPin(certificate: X509Certificate): String {
        return publicKeyPin(certificate.publicKey)
    }

    @JvmStatic
    internal fun publicKeyPin(publicKey: PublicKey): String {
        val hash = MessageDigest.getInstance("SHA-256").digest(publicKey.encoded)
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    @JvmStatic
    internal fun hasPrimaryPin(hostname: String, pin: String): Boolean =
        pinsForHost(hostname)?.primary == pin

    @JvmStatic
    internal fun hasBackupPin(hostname: String, pin: String): Boolean =
        pinsForHost(hostname)?.backup == pin

    internal fun pinInventory(): Map<String, Set<String>> =
        pinsByHost.mapValues { (_, pinSet) -> pinSet.all }
}
