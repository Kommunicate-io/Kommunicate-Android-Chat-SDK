package io.kommunicate.network;

import static com.google.gson.internal.$Gson$Types.arrayOf;

import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Objects;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SSLPinningConfig {

    public synchronized static SSLSocketFactory createPinnedSSLSocketFactory() {
        String expectedPublicKeyHash = "";

        TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                // No client side authentication implemented
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                if (chain.length == 0) {
                    throw new CertificateException("Certificate chain is empty");
                }

                X509Certificate certificate = chain[0];
                PublicKey publicKey = certificate.getPublicKey();
                byte[] publicKeyBytes = publicKey.getEncoded();

                MessageDigest md = null;
                try {
                    md = MessageDigest.getInstance("SHA-256");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                byte[] publicKeyHash = md.digest(publicKeyBytes);
                String publicKeyHashBase64 = android.util.Base64.encodeToString(publicKeyHash, android.util.Base64.NO_WRAP);

                if (!Objects.equals(publicKeyHashBase64, expectedPublicKeyHash)) {
                    throw new CertificateException("Public key pinning failure");
                }
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };

        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[] {trustManager}, null);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return sslContext.getSocketFactory();
    }
}
