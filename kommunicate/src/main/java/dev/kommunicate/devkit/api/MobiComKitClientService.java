package dev.kommunicate.devkit.api;

import android.content.Context;
import android.text.TextUtils;

import dev.kommunicate.devkit.Applozic;
import dev.kommunicate.devkit.api.account.user.MobiComUserPreference;

import dev.kommunicate.commons.ALSpecificSettings;
import dev.kommunicate.commons.ApplozicService;
import dev.kommunicate.commons.commons.core.utils.Utils;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import io.kommunicate.BuildConfig;
import io.kommunicate.network.SSLPinningConfig;

/**
 * Created by devashish on 27/12/14.
 */
public class MobiComKitClientService {

    public static final String BASE_URL_METADATA = "com.applozic.server.url";
    public static final String KM_BASE_URL_METADATA = "io.kommunicate.server.url";
    public static final String MQTT_BASE_URL_METADATA = "com.applozic.mqtt.server.url";
    public static final String FILE_URL = "/rest/ws/aws/file/";
    public static String APPLICATION_KEY_HEADER = "Application-Key";
    public static String APP_MOUDLE_NAME_KEY_HEADER = "App-Module-Name";
    public static String APPLICATION_KEY_HEADER_VALUE_METADATA = "com.applozic.application.key";
    public static String APP_MODULE_NAME_META_DATA_KEY = "com.applozic.module.key";
    protected Context context;
    protected String DEFAULT_MQTT_URL = "tcp://apps.applozic.com:1883";
    public static String FILE_BASE_URL_METADATA_KEY = "com.applozic.attachment.url";
    public static String FILE_UPLOAD_METADATA_KEY = "com.applozic.attachment.upload.endpoint";
    public static String FILE_DOWNLOAD_METADATA_KEY = "com.applozic.attachment.download.endpoint";
    public static final String conn_err = "Error connecting";
    public static final String NOT_HTTP_CONN = "Not an HTTP connection";

    public MobiComKitClientService() {

    }

    public MobiComKitClientService(Context context) {
        this.context = ApplozicService.getContext(context);
    }

    public static String getApplicationKey(Context context) {
        String applicationKey = Applozic.getInstance(ApplozicService.getContext(context)).getApplicationKey();
        if (!TextUtils.isEmpty(applicationKey)) {
            return applicationKey;
        }
        return Utils.getMetaDataValue(ApplozicService.getContext(context), APPLICATION_KEY_HEADER_VALUE_METADATA);
    }

    public static String getAppModuleName(Context context) {
        return Utils.getMetaDataValue(ApplozicService.getContext(context), APP_MODULE_NAME_META_DATA_KEY);
    }

    public String getBaseUrl() {
        String SELECTED_BASE_URL = MobiComUserPreference.getInstance(context).getUrl();

        if (!TextUtils.isEmpty(SELECTED_BASE_URL)) {
            return SELECTED_BASE_URL;
        } else {
            String alCustomUrl = ALSpecificSettings.getInstance(context).getAlBaseUrl();
            if (!TextUtils.isEmpty(alCustomUrl)) {
                return alCustomUrl;
            }
        }

        String BASE_URL = Utils.getMetaDataValue(context.getApplicationContext(), BASE_URL_METADATA);
        if (!TextUtils.isEmpty(BASE_URL)) {
            return BASE_URL;
        }

        return BuildConfig.CHAT_SERVER_URL;
    }

    public String getKmBaseUrl() {
        String kmCustomUrl = ALSpecificSettings.getInstance(context).getKmBaseUrl();

        if (!TextUtils.isEmpty(kmCustomUrl)) {
            return kmCustomUrl;
        }

        String KM_BASE_URL = Utils.getMetaDataValue(context.getApplicationContext(), KM_BASE_URL_METADATA);
        if (!TextUtils.isEmpty(KM_BASE_URL)) {
            return KM_BASE_URL;
        }

        return BuildConfig.API_SERVER_URL;
    }

    protected String getMqttBaseUrl() {
        String MQTT_BROKER_URL = MobiComUserPreference.getInstance(context).getMqttBrokerUrl();
        if (!TextUtils.isEmpty(MQTT_BROKER_URL)) {
            return MQTT_BROKER_URL;
        }
        String MQTT_BASE_URL = Utils.getMetaDataValue(context.getApplicationContext(), MQTT_BASE_URL_METADATA);
        if (!TextUtils.isEmpty(MQTT_BASE_URL)) {
            return MQTT_BASE_URL;
        }
        return BuildConfig.MQTT_URL;
    }

    public PasswordAuthentication getCredentials() {
        MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(context);
        if (!userPreferences.isRegistered()) {
            return null;
        }
        return new PasswordAuthentication(userPreferences.getUserId(), userPreferences.getDeviceKeyString().toCharArray());
    }

    public HttpURLConnection openHttpConnection(String urlString) throws IOException {
        HttpURLConnection httpConn;

        URL url = new URL(urlString);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

        if (conn == null)
            throw new IOException(NOT_HTTP_CONN);

        try {
            conn.setSSLSocketFactory(SSLPinningConfig.createPinnedSSLSocketFactory());
            conn.setAllowUserInteraction(false);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestMethod("GET");
            conn.connect();
        } catch (Exception ex) {
            throw new IOException(conn_err);
        }
        return conn;
    }

    public String getFileUrl() {
        String fileDownloadURL = Utils.getMetaDataValue(context.getApplicationContext(), FILE_DOWNLOAD_METADATA_KEY);
        if (!TextUtils.isEmpty(fileDownloadURL)) {
            return getFileBaseUrl() + fileDownloadURL;
        }
        return getFileBaseUrl() + FILE_URL;
    }

    public String getFileBaseUrl() {
        String fileURL = Utils.getMetaDataValue(context.getApplicationContext(), FILE_BASE_URL_METADATA_KEY);
        return (TextUtils.isEmpty(fileURL) ? BuildConfig.CHAT_SERVER_URL : fileURL);
    }
}
