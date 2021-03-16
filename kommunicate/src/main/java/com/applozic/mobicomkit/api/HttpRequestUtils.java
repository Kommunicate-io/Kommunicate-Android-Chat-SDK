package com.applozic.mobicomkit.api;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.account.register.RegisterUserClientService;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.api.authentication.AlAuthService;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.encryption.EncryptionUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


/**
 * Created by devashish on 28/11/14.
 */
public class HttpRequestUtils {

    private static final String TAG = "HttpRequestUtils";
    public static String APP_MODULE_NAME_KEY_HEADER = "App-Module-Name";
    private static final String OF_USER_ID_HEADER = "Of-User-Id";
    private static final String X_AUTHORIZATION_HEADER = "x-authorization";
    private static final String APZ_APP_ID_HEADER = "Apz-AppId";
    public static String APPLICATION_KEY_HEADER = "Application-Key";
    public static String DEVICE_KEY_HEADER = "Device-Key";
    private static final String APZ_PRODUCT_APP_HEADER = "Apz-Product-App";
    public static boolean isRefreshTokenInProgress = false;
    private Context context;


    public HttpRequestUtils(Context context) {
        this.context = ApplozicService.getContext(context);
    }

    public String postData(String urlString, String contentType, String accept, String data) throws Exception {
        return postData(urlString, contentType, accept, data, null);
    }

    public String postDataForAuthToken(String urlString, String contentType, String accept, String data, String userId) throws Exception {
        Utils.printLog(context, TAG, "Calling url: " + urlString);
        HttpURLConnection connection;
        URL url;
        try {
            if (!TextUtils.isEmpty(MobiComUserPreference.getInstance(context).getEncryptionKey())) {
                data = EncryptionUtils.encrypt(MobiComUserPreference.getInstance(context).getEncryptionKey(), data);
            }
            url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            if (!TextUtils.isEmpty(contentType)) {
                connection.setRequestProperty("Content-Type", contentType);
            }
            if (!TextUtils.isEmpty(accept)) {
                connection.setRequestProperty("Accept", accept);
            }
            addHeadersForAuthToken(connection, userId);
            connection.connect();

            if (connection == null) {
                return null;
            }
            if (data != null) {
                byte[] dataBytes = data.getBytes("UTF-8");
                DataOutputStream os = new DataOutputStream(connection.getOutputStream());
                os.write(dataBytes);
                os.flush();
                os.close();
            }
            BufferedReader br = null;
            if (connection.getResponseCode() == 200 || connection.getResponseCode() == 201) {
                InputStream inputStream = connection.getInputStream();
                br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            }
            StringBuilder sb = new StringBuilder();
            try {
                String line;
                if (br != null) {
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    br.close();
                }
            }
            Utils.printLog(context, TAG, "Response : " + sb.toString());
            if (!TextUtils.isEmpty(sb.toString())) {
                if (!TextUtils.isEmpty(MobiComUserPreference.getInstance(context).getEncryptionKey())) {
                    return EncryptionUtils.decrypt(MobiComUserPreference.getInstance(context).getEncryptionKey(), sb.toString());
                }
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isRefreshTokenInProgress = false;
        }
        Utils.printLog(context, TAG, "Http call failed");
        return null;
    }

    public String postData(String urlString, String contentType, String accept, String data, String userId) throws Exception {
        Utils.printLog(context, TAG, "Calling url: " + urlString);
        HttpURLConnection connection;
        URL url;
        try {
            if (!TextUtils.isEmpty(MobiComUserPreference.getInstance(context).getEncryptionKey())) {
                data = EncryptionUtils.encrypt(MobiComUserPreference.getInstance(context).getEncryptionKey(), data);
            }
            url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            if (!TextUtils.isEmpty(contentType)) {
                connection.setRequestProperty("Content-Type", contentType);
            }
            if (!TextUtils.isEmpty(accept)) {
                connection.setRequestProperty("Accept", accept);
            }
            addGlobalHeaders(connection, userId);
            connection.connect();

            if (connection == null) {
                return null;
            }
            if (data != null) {
                byte[] dataBytes = data.getBytes("UTF-8");
                DataOutputStream os = new DataOutputStream(connection.getOutputStream());
                os.write(dataBytes);
                os.flush();
                os.close();
            }
            BufferedReader br = null;
            if (connection.getResponseCode() == 200 || connection.getResponseCode() == 201) {
                InputStream inputStream = connection.getInputStream();
                br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            }
            StringBuilder sb = new StringBuilder();
            try {
                String line;
                if (br != null) {
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    br.close();
                }
            }
            Utils.printLog(context, TAG, "Response : " + sb.toString());
            if (!TextUtils.isEmpty(sb.toString())) {
                if (!TextUtils.isEmpty(MobiComUserPreference.getInstance(context).getEncryptionKey())) {
                    return EncryptionUtils.decrypt(MobiComUserPreference.getInstance(context).getEncryptionKey(), sb.toString());
                }
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isRefreshTokenInProgress = false;
        }
        Utils.printLog(context, TAG, "Http call failed");
        return null;
    }

    public String postJsonToServer(String stringUrl, String data) throws Exception {
        return postJsonToServer(stringUrl, data, null);
    }

    public String postJsonToServer(String stringUrl, String data, String userId) throws Exception {
        HttpURLConnection connection;
        URL url = new URL(stringUrl);
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        addGlobalHeaders(connection, userId);
        connection.connect();

        byte[] dataBytes = data.getBytes("UTF-8");
        DataOutputStream os = new DataOutputStream(connection.getOutputStream());
        os.write(dataBytes);
        os.flush();
        os.close();
        BufferedReader br = null;
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStream inputStream = connection.getInputStream();
            br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        } else {
            Utils.printLog(context, TAG, "Response code for post json is :" + connection.getResponseCode());
        }
        StringBuilder sb = new StringBuilder();
        try {
            String line;
            if (br != null) {
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable e) {
        } finally {
            if (br != null) {
                br.close();
            }
        }
        Utils.printLog(context, TAG, "Response: " + sb.toString());
        return sb.toString();
    }

    public String getResponse(String urlString, String contentType, String accept) {
        return getResponse(urlString, contentType, accept, false, null);
    }

    public String getResponse(String urlString, String contentType, String accept, boolean isFileUpload) {
        return getResponse(urlString, contentType, accept, isFileUpload, null);
    }

    public String getResponseWithException(String urlString, String contentType, String accept, boolean isFileUpload, String userId) throws Exception {
        Utils.printLog(context, TAG, "Calling url: " + urlString);

        HttpURLConnection connection = null;
        URL url;

        try {
            url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setDoInput(true);

            if (!TextUtils.isEmpty(contentType)) {
                connection.setRequestProperty("Content-Type", contentType);
            }
            if (!TextUtils.isEmpty(accept)) {
                connection.setRequestProperty("Accept", accept);
            }
            addGlobalHeaders(connection, userId);
            connection.connect();

            if (connection == null) {
                return null;
            }
            BufferedReader br = null;
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            } else {
                Utils.printLog(context, TAG, "Response code for getResponse is  :" + connection.getResponseCode());
            }

            StringBuilder sb = new StringBuilder();
            try {
                String line;
                if (br != null) {
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            } finally {
                if (br != null) {
                    br.close();
                }
            }

            Utils.printLog(context, TAG, "Response :" + sb.toString());

            if (!TextUtils.isEmpty(sb.toString())) {
                if (!TextUtils.isEmpty(MobiComUserPreference.getInstance(context).getEncryptionKey())) {
                    return isFileUpload ? sb.toString() : EncryptionUtils.decrypt(MobiComUserPreference.getInstance(context).getEncryptionKey(), sb.toString());
                }
            }
            return sb.toString();
        } catch (ConnectException e) {
            Utils.printLog(context, TAG, "failed to connect Internet is not working");
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } catch (Throwable e) {
            throw e;
        } finally {
            isRefreshTokenInProgress = false;
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            }
        }
    }

    public String getResponse(String urlString, String contentType, String accept, boolean isFileUpload, String userId) {
        Utils.printLog(context, TAG, "Calling url: " + urlString);

        HttpURLConnection connection = null;
        URL url;

        try {
            url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setDoInput(true);

            if (!TextUtils.isEmpty(contentType)) {
                connection.setRequestProperty("Content-Type", contentType);
            }
            if (!TextUtils.isEmpty(accept)) {
                connection.setRequestProperty("Accept", accept);
            }
            addGlobalHeaders(connection, userId);
            connection.connect();

            if (connection == null) {
                return null;
            }
            BufferedReader br = null;
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            } else {
                Utils.printLog(context, TAG, "Response code for getResponse is  :" + connection.getResponseCode());
            }

            StringBuilder sb = new StringBuilder();
            try {
                String line;
                if (br != null) {
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    br.close();
                }
            }

            Utils.printLog(context, TAG, "Response :" + sb.toString());

            if (!TextUtils.isEmpty(sb.toString())) {
                if (!TextUtils.isEmpty(MobiComUserPreference.getInstance(context).getEncryptionKey())) {
                    return isFileUpload ? sb.toString() : EncryptionUtils.decrypt(MobiComUserPreference.getInstance(context).getEncryptionKey(), sb.toString());
                }
            }
            return sb.toString();
        } catch (ConnectException e) {
            Utils.printLog(context, TAG, "failed to connect Internet is not working");
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable e) {

        } finally {
            isRefreshTokenInProgress = false;
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public void addGlobalHeaders(HttpURLConnection connection, String userId) {
        try {
            if (MobiComKitClientService.getAppModuleName(context) != null) {
                connection.setRequestProperty(APP_MODULE_NAME_KEY_HEADER, MobiComKitClientService.getAppModuleName(context));
            }

            if (!TextUtils.isEmpty(userId)) {
                connection.setRequestProperty(OF_USER_ID_HEADER, URLEncoder.encode(userId, "UTF-8"));
            }
            String applicationKey = MobiComKitClientService.getApplicationKey(context);

            MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(context);

            if (User.RoleType.AGENT.getValue().equals(userPreferences.getUserRoleType()) && !TextUtils.isEmpty(userId)) {
                connection.setRequestProperty(APZ_APP_ID_HEADER, applicationKey);
                connection.setRequestProperty(APZ_PRODUCT_APP_HEADER, "true");
            } else {
                connection.setRequestProperty(APPLICATION_KEY_HEADER, applicationKey);
            }

            if (!AlAuthService.isTokenValid(context) && !isRefreshTokenInProgress) {
                new RegisterUserClientService(context).refreshAuthToken(applicationKey, userPreferences.getUserId());
            }

            String userAuthToken = userPreferences.getUserAuthToken();
            if (userPreferences.isRegistered() && !TextUtils.isEmpty(userAuthToken)) {
                connection.setRequestProperty(X_AUTHORIZATION_HEADER, userAuthToken);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isRefreshTokenInProgress = false;
        }
    }

    public void addHeadersForAuthToken(HttpURLConnection connection, String userId) {
        try {
            if (MobiComKitClientService.getAppModuleName(context) != null) {
                connection.setRequestProperty(APP_MODULE_NAME_KEY_HEADER, MobiComKitClientService.getAppModuleName(context));
            }

            if (!TextUtils.isEmpty(userId)) {
                connection.setRequestProperty(OF_USER_ID_HEADER, URLEncoder.encode(userId, "UTF-8"));
            }
            String applicationKey = MobiComKitClientService.getApplicationKey(context);

            MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(context);

            if (User.RoleType.AGENT.getValue().equals(userPreferences.getUserRoleType()) && !TextUtils.isEmpty(userId)) {
                connection.setRequestProperty(APZ_APP_ID_HEADER, applicationKey);
                connection.setRequestProperty(APZ_PRODUCT_APP_HEADER, "true");
            } else {
                connection.setRequestProperty(APPLICATION_KEY_HEADER, applicationKey);
            }
            connection.setRequestProperty(DEVICE_KEY_HEADER, userPreferences.getDeviceKeyString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isRefreshTokenInProgress = false;
        }
    }
}
