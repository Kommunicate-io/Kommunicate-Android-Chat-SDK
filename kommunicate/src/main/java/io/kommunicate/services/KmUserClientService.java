package io.kommunicate.services;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.HttpRequestUtils;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.UserClientService;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.encryption.EncryptionUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import io.kommunicate.users.KMUser;

/**
 * Created by ashish on 30/01/18.
 */

public class KmUserClientService extends UserClientService {

    private static final String USER_LIST_FILTER_URL = "/rest/ws/user/v3/filter?startIndex=";
    private static final String USER_LOGIN_API = "/login";
    private static final String USER_SIGNUP_API = "/customers?preSignUp=";
    private static final String KM_PROD_BASE_URL = "https://api.kommunicate.io";
    private static final String KM_TEST_BASE_URL = "https://api-test.kommunicate.io";
    private static final String CREATE_CONVERSATION_URL = "/conversations";
    private static final String KM_GET_HELPDOCS_KEY_URL = "/integration/settings/";
    private static final String KM_HELPDOCS_URL = "https://api.helpdocs.io/v1/article";
    private static final String KM_HELPDOCS_SERACH_URL = "https://api.helpdocs.io/v1/search?key=";
    private HttpRequestUtils httpRequestUtils;
    private static String TAG = "KmUserClientService";

    public KmUserClientService(Context context) {
        super(context);
        httpRequestUtils = new HttpRequestUtils(context);
    }

    private String getUserListFilterUrl() {
        return getBaseUrl() + USER_LIST_FILTER_URL;
    }

    private String getKmBaseUrl() {
        if (getBaseUrl().contains("apps.applozic")) {
            return KM_PROD_BASE_URL;
        }
        return KM_TEST_BASE_URL;
    }

    private String getArticleListUrl() {
        return KM_HELPDOCS_URL + "?key=";
    }

    private String getKmGetHelpdocsKeyUrl() {
        return getKmBaseUrl() + KM_GET_HELPDOCS_KEY_URL;
    }

    private String getCreateConversationUrl() {
        return getKmBaseUrl() + CREATE_CONVERSATION_URL;
    }

    private String getLoginUserUrl() {
        return getKmBaseUrl() + USER_LOGIN_API;
    }

    public String getUserListFilter(List<String> roleList, int startIndex, int pageSize) {
        StringBuilder urlBuilder = new StringBuilder(getUserListFilterUrl());

        urlBuilder.append(startIndex);
        urlBuilder.append("&pageSize=");
        urlBuilder.append(pageSize);

        if (roleList != null && !roleList.isEmpty()) {
            for (String role : roleList) {
                urlBuilder.append("&");
                urlBuilder.append("roleNameList=");
                urlBuilder.append(role);
            }
        }

        String response = httpRequestUtils.getResponse(urlBuilder.toString(), "application/json", "application/json");

        return response;
    }

    public String createConversation(Integer groupId, String userId, String agentId, String applicationId) {

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("groupId", groupId);
            jsonObject.put("participentUserId", userId);
            jsonObject.put("createdBy", userId);
            jsonObject.put("defaultAgentId", agentId);
            jsonObject.put("applicationId", applicationId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            String response = postData(getCreateConversationUrl(), "application/json", "application/json", jsonObject.toString());
            Utils.printLog(context, TAG, "Response : " + response);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getHelpDocsKey(String appKey, String type) throws Exception {
        StringBuilder urlBuilder = new StringBuilder(getKmGetHelpdocsKeyUrl());

        if (!TextUtils.isEmpty(appKey)) {
            urlBuilder.append(appKey);
        }
        if (!TextUtils.isEmpty(type)) {
            urlBuilder.append("?type=");
            urlBuilder.append(type);
        }

        try {
            return httpRequestUtils.getResponse(urlBuilder.toString(), "application/json", "application/json");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getArticleList(String helpDocsKey) throws Exception {
        StringBuilder urlBuilder = new StringBuilder(KM_HELPDOCS_URL);

        if (!TextUtils.isEmpty(helpDocsKey)) {
            urlBuilder.append("?key=");
            urlBuilder.append(helpDocsKey);
        }

        try {
            return getResponse(urlBuilder.toString(), "application/json", "application/json");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getSelectedArticles(String helpDocsKey, String queryString) throws Exception {
        StringBuilder urlBuilder = new StringBuilder(KM_HELPDOCS_SERACH_URL);
        urlBuilder.append(helpDocsKey);
        urlBuilder.append("&query=");
        urlBuilder.append(queryString);

        try {
            return getResponse(urlBuilder.toString(), "application/json", "application/json");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getArticleAnswer(String articleId, String helpDocsKey) throws Exception {
        StringBuilder urlBuilder = new StringBuilder(KM_HELPDOCS_URL);
        urlBuilder.append("/");
        urlBuilder.append(articleId);
        urlBuilder.append("?key=");
        urlBuilder.append(helpDocsKey);

        try {
            return getResponse(urlBuilder.toString(), "application/json", "application/json");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getDashboardFaq(String appKey, String articleId) throws Exception {
        StringBuilder urlBuilder = new StringBuilder(getKmBaseUrl());
        urlBuilder.append("/kb/search?appId=");
        urlBuilder.append(appKey);
        if (!TextUtils.isEmpty(articleId)) {
            urlBuilder.append("&articleId=");
            urlBuilder.append(articleId);
        }

        try {
            return getResponse(urlBuilder.toString(), "application/json", "application/json");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String postData(String urlString, String contentType, String accept, String data) throws Exception {
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
            }
            Utils.printLog(context, TAG, "Response : " + sb.toString());
            if (!TextUtils.isEmpty(sb.toString())) {
                if (!TextUtils.isEmpty(MobiComUserPreference.getInstance(context).getEncryptionKey())) {
                    if (connection != null) {
                        connection.disconnect();
                    }
                    return EncryptionUtils.decrypt(MobiComUserPreference.getInstance(context).getEncryptionKey(), sb.toString());
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Utils.printLog(context, TAG, "Http call failed");
        return null;
    }

    public String getResponse(String urlString, String contentType, String accept) {
        Utils.printLog(context, TAG, "Calling url: " + urlString);
        boolean isFileUpload = false;

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

    public String loginKmUser(KMUser user) {
        return null;
    }
}
