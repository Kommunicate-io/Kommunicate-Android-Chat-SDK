package io.kommunicate.network

import android.annotation.SuppressLint
import android.util.Base64
import io.kommunicate.BuildConfig
import io.kommunicate.utils.KmUtils
import java.security.KeyManagementException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object SSLPinningConfig {

    @JvmStatic
    @Synchronized
    fun createPinnedSSLSocketFactory(): SSLSocketFactory {
        if(KmUtils.isDeviceRooted()) {
            throw CertificateException("Unable to Establish Connection, Device is rooted.")
        }

        val expectedPublicKeyHash = arrayOf(
            BuildConfig.apiKommunicateIo,
            BuildConfig.apiEuKommunicateIo,
            BuildConfig.apiCaKommunicateIo,
            BuildConfig.apiTestKommuncateIo,
            BuildConfig.kommunicateIo,
            BuildConfig.apiInKommunicateIo
        )

        val trustManager: TrustManager = @SuppressLint("CustomX509TrustManager")
        object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                // No client side authentication implemented
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                if (chain.isEmpty()) {
                    throw CertificateException("Certificate chain is empty")
                }

                val certificate = chain[0]
                val publicKeyBytes = certificate.publicKey.encoded

                val md: MessageDigest = try {
                    MessageDigest.getInstance("SHA-256")
                } catch (e: NoSuchAlgorithmException) {
                    e.printStackTrace()
                    throw RuntimeException(e)
                }

                val publicKeyHash = md.digest(publicKeyBytes)
                val publicKeyHashBase64 = Base64.encodeToString(publicKeyHash, Base64.NO_WRAP)

                if (!expectedPublicKeyHash.contains(publicKeyHashBase64)) {
                    throw CertificateException("Public key pinning failure")
                }
            }

            override fun getAcceptedIssuers(): Array<X509Certificate?> {
                return arrayOfNulls(0)
            }
        }

        var sslContext: SSLContext? = null
        try {
            sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf(trustManager), null)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            throw RuntimeException(e)
        } catch (e: KeyManagementException) {
            e.printStackTrace()
            throw RuntimeException(e)
        }

        return sslContext.socketFactory
    }
}
