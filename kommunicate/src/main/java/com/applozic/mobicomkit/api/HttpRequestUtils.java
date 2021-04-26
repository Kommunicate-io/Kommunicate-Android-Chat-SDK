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
    private String encryptionKey;
    private Context context;


    public HttpRequestUtils(Context context) {
        this.context = ApplozicService.getContext(context);
        this.encryptionKey = MobiComUserPreference.getInstance(context).getEncryptionKey();
    }

    public String postData(String urlString, String data) throws Exception {
        return postData(urlString, data, null, false, false, null, null);
    }

    public String postData(String urlString, String data, String userId) throws Exception {
        return postData(urlString, data, userId, false, false, null, null);
    }

    public String postData(String urlString, String contentType, String accept, String data) throws Exception {
        return postData(urlString, data, null, false, false, accept, contentType);
    }

    public String postJsonToServer(String stringUrl, String data) throws Exception {
        return postData(stringUrl, data, null, false, false, null, null);
    }

    public String makePatchRequest(String url, String data) throws Exception {
        return postData(url, data, null, true, false, null, null);
    }

    public String getResponse(String urlString, String contentType, String accept) {
        try {
            return getResponseWithException(urlString, contentType, accept, false, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getResponse(String urlString, String contentType, String accept, boolean isFileUpload) {
        try {
            return getResponseWithException(urlString, contentType, accept, isFileUpload, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getResponse(String urlString) throws Exception {
        return getResponseWithException(urlString, null, null, false, null);
    }

    public String postDataForAuthToken(String urlString, String data, String userId) throws Exception {
        return postData(urlString, data, userId, false, true, null, null);
    }

    public String postData(String urlString, String data, String userId, boolean isPatchRequest, boolean isForAuth, String accept, String contentType) throws Exception {
        Utils.printLog(context, TAG, (isPatchRequest ? "\n\n** Patching data ** : " : "\n\n** Posting data **: ") + data + "\nTo URL: " + urlString + "\n\n");
        HttpURLConnection connection;
        URL url;
        try {
            if (!TextUtils.isEmpty(encryptionKey)) {
                data = EncryptionUtils.encrypt(encryptionKey, data);
            }
            url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(isPatchRequest ? "PATCH" : "POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            connection.setRequestProperty("Content-Type", !TextUtils.isEmpty(contentType) ? contentType : "application/json");
            connection.setRequestProperty("Accept", !TextUtils.isEmpty(accept) ? accept : "application/json");

            if (isForAuth) {
                addHeadersForAuthToken(connection, userId);
            } else {
                addGlobalHeaders(connection, userId);
            }
            connection.connect();

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
            } else {
                Utils.printLog(context, TAG, "\n\nResponse code for url: " + urlString + "\n** Code ** : " + connection.getResponseCode() + "\n\n");
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
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            } finally {
                if (br != null) {
                    br.close();
                }
            }
            Utils.printLog(context, TAG, "\n\nResponse for url: " + urlString + "\n** Response** : " + sb.toString() + "\n\n");
            if (!TextUtils.isEmpty(sb.toString()) && !TextUtils.isEmpty(encryptionKey)) {
                return EncryptionUtils.decrypt(encryptionKey, sb.toString());
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            isRefreshTokenInProgress = false;
        }
    }

    public String getResponseWithException(String urlString, String contentType, String accept, boolean isFileUpload, String userId) throws Exception {
        Utils.printLog(context, TAG, "Calling url **[GET]** : " + urlString);

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

            BufferedReader br = null;
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            } else {
                Utils.printLog(context, TAG, "\n\nResponse code for url: " + urlString + "\n** Code ** : " + connection.getResponseCode() + "\n\n");
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

            Utils.printLog(context, TAG, "\n\nGET Response for url: " + urlString + "\n** Response **: " + sb.toString() + "\n\n");

            if (!TextUtils.isEmpty(sb.toString()) && !TextUtils.isEmpty(encryptionKey)) {
                return isFileUpload ? sb.toString() : EncryptionUtils.decrypt(encryptionKey, sb.toString());
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
                connection.disconnect();
            }
        }
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
