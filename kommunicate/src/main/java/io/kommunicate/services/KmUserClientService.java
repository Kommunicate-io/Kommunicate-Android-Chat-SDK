package io.kommunicate.services;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import io.kommunicate.devkit.KommunicateSettings;
import io.kommunicate.devkit.SettingsSharedPreference;
import io.kommunicate.devkit.api.HttpRequestUtils;
import io.kommunicate.devkit.api.account.register.RegistrationResponse;
import io.kommunicate.devkit.api.account.user.MobiComUserPreference;
import io.kommunicate.devkit.api.account.user.User;
import io.kommunicate.devkit.api.account.user.UserClientService;
import io.kommunicate.devkit.api.conversation.MqttIntentService;
import io.kommunicate.devkit.api.conversation.ConversationIntentService;
import io.kommunicate.devkit.api.notification.NotificationChannels;
import io.kommunicate.devkit.contact.AppContactService;
import io.kommunicate.devkit.exception.InvalidApplicationException;
import io.kommunicate.devkit.exception.UnAuthoriseException;
import io.kommunicate.commons.commons.core.utils.Utils;
import io.kommunicate.commons.encryption.EncryptionUtils;
import io.kommunicate.commons.json.GsonUtils;
import io.kommunicate.commons.people.contact.Contact;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.TimeZone;

import io.kommunicate.BuildConfig;
import io.kommunicate.KMGroupInfo;
import io.kommunicate.KmException;
import io.kommunicate.feeds.KmRegistrationResponse;
import io.kommunicate.network.SSLPinningConfig;
import io.kommunicate.users.KMUser;

import static io.kommunicate.devkit.api.HttpRequestUtils.DEVICE_KEY_HEADER;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by ashish on 30/01/18.
 */

public class KmUserClientService extends UserClientService {
    private static String TAG = "KmUserClientService";
    private static final String INVALID_USERNAME_PASSWORD = "Invalid uername/password";
    public static final String AUTHORIZATION = "Authorization";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String ACCEPT = "Accept";
    public static final String GET = "GET";
    public static final String UTF_8 = "UTF-8";
    private static final String INVALID_RESPONSE = "Invalid response";
    private static final String USER_LIST_FILTER_URL = "/rest/ws/user/v3/filter?startIndex=";
    private static final String USER_LOGIN_API = "/login";
    private static final String GET_APPLICATION_LIST = "/rest/ws/user/getlist";
    private static final String CONVERSATION_URL = "/conversations";
    private static final String KM_GET_HELPDOCS_KEY_URL = "/integration/settings/";
    private static final String USER_PASSWORD_RESET = "/users/password-reset";
    private static final String INVALID_APP_ID = "INVALID_APPLICATIONID";
    private static final String CREATE_CONVERSATION_URL = "/create";
    private static final String BOTS_BASE_URL = "/rest/ws/botdetails/";
    private static final String GET_AGENT_DETAILS = "/users/list";
    public HttpRequestUtils httpRequestUtils;
    private static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    private static final String INVALID_APPLI_ID = "Invalid Application Id";
    private static final String SERVICE_UNAVAILABLE = "503 Service Unavailable";
    private static final String APPLI_JSON = "application/json";
    private static final String NO_INTERNET_CONN = "No Internet Connection";
    private static final String APPLICATION_WEB_ADMIN = "?roleNameList=APPLICATION_WEB_ADMIN&";
    private static final String KB_SEARCH_ID = "/kb/search?appId=";
    private static final String CHANNELINFO_NOT_NULL = "ChannelInfo cannot be null";

    public KmUserClientService(Context context) {
        super(context);
        httpRequestUtils = new HttpRequestUtils(context);
    }

    private String getUserListFilterUrl() {
        return getBaseUrl() + USER_LIST_FILTER_URL;
    }

    private String getArticleListUrl() {
        return BuildConfig.HELPCENTER_URL + "?key=";
    }

    private String getKmGetHelpdocsKeyUrl() {
        return getKmBaseUrl() + KM_GET_HELPDOCS_KEY_URL;
    }

    private String getConversationUrl() {
        return getKmBaseUrl() + CONVERSATION_URL;
    }

    private String getCreateConversationUrl() {
        return getConversationUrl() + CREATE_CONVERSATION_URL;
    }

    private String getAgentListUrl() {
        return getKmBaseUrl() + KmClientService.APP_SETTING_URL;
    }

    private String getApplicationListUrl() {
        return getBaseUrl() + GET_APPLICATION_LIST;
    }

    private String getLoginUserUrl() {
        return getKmBaseUrl() + USER_LOGIN_API;
    }

    public String getUserListFilter(List<String> roleList, int startIndex, int pageSize, int orderBy) throws Exception {
        try {
            StringBuilder urlBuilder = new StringBuilder(getUserListFilterUrl());

            urlBuilder.append(startIndex).append("&pageSize=").append(pageSize).append("&orderBy=").append(orderBy);

            if (roleList != null && !roleList.isEmpty()) {
                for (String role : roleList) {
                    urlBuilder.append("&").append("roleNameList=").append(role);
                }
            }

            return httpRequestUtils.getResponse(urlBuilder.toString(), APPLI_JSON, APPLI_JSON);
        } catch (Exception e) {
            throw e;
        }
    }

    public String createConversation(Integer groupId, String userId, String agentId, String applicationId) {

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("groupId", groupId);
            jsonObject.put("participantUserId", userId);
            jsonObject.put("createdBy", userId);
            jsonObject.put("defaultAgentId", agentId);
            jsonObject.put("applicationId", applicationId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            String response = httpRequestUtils.postData(getConversationUrl(), APPLI_JSON, APPLI_JSON, jsonObject.toString());
            Utils.printLog(context, TAG, "Response : " + response);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String createConversation(KMGroupInfo channelInfo) throws Exception {
        if (channelInfo == null) {
            throw new KmException(CHANNELINFO_NOT_NULL);
        }

        String channelJson = GsonUtils.getJsonFromObject(channelInfo, KMGroupInfo.class);
        return httpRequestUtils.postData(getCreateConversationUrl(), APPLI_JSON, APPLI_JSON, channelJson);
    }

    public String getHelpDocsKey(String appKey, String type) throws Exception {
        StringBuilder urlBuilder = new StringBuilder(getKmGetHelpdocsKeyUrl());

        if (!TextUtils.isEmpty(appKey)) {
            urlBuilder.append(appKey);
        }
        if (!TextUtils.isEmpty(type)) {
            urlBuilder.append("?type=").append(type);
        }

        try {
            return httpRequestUtils.getResponse(urlBuilder.toString(), APPLI_JSON, APPLI_JSON);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getArticleList(String helpDocsKey) throws Exception {
        StringBuilder urlBuilder = new StringBuilder(BuildConfig.HELPCENTER_URL);

        if (!TextUtils.isEmpty(helpDocsKey)) {
            urlBuilder.append("?key=").append(helpDocsKey);
        }

        try {
            return getResponse(urlBuilder.toString(), APPLI_JSON, APPLI_JSON);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getSelectedArticles(String helpDocsKey, String queryString) throws Exception {
        StringBuilder urlBuilder = new StringBuilder(BuildConfig.HELPCENTER_URL);
        urlBuilder.append(helpDocsKey).append("&q=").append(queryString);

        try {
            return getResponse(urlBuilder.toString(), APPLI_JSON, APPLI_JSON);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getArticleAnswer(String articleId, String helpDocsKey) throws Exception {
        StringBuilder urlBuilder = new StringBuilder(BuildConfig.HELPCENTER_URL);
        urlBuilder.append("/").append(articleId).append("?key=").append(helpDocsKey);

        try {
            return getResponse(urlBuilder.toString(), APPLI_JSON, APPLI_JSON);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getDashboardFaq(String appKey, String articleId) throws Exception {
        StringBuilder urlBuilder = new StringBuilder(getKmBaseUrl());
        urlBuilder.append(KB_SEARCH_ID).append(appKey);
        if (!TextUtils.isEmpty(articleId)) {
            urlBuilder.append("&articleId=").append(articleId);
        }

        try {
            return getResponse(urlBuilder.toString(), APPLI_JSON, APPLI_JSON);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getApplicationList(String userId, boolean isEmailId) {
        if (TextUtils.isEmpty(userId)) {
            return null;
        }
        try {
            String url = getApplicationListUrl() + APPLICATION_WEB_ADMIN + (isEmailId ? "emailId=" : "userId=") + URLEncoder.encode(userId, "UTF-8");
            return httpRequestUtils.getResponse(url, APPLI_JSON, APPLI_JSON);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String resetUserPassword(String userId, String appKey) {
        if (userId == null || appKey == null) {
            return null;
        }

        String url = getKmBaseUrl() + USER_PASSWORD_RESET;

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userName", userId);
            jsonObject.put("applicationId", appKey);

            return httpRequestUtils.postJsonToServer(url, jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getAgentList(String appKey) throws KmException {
        try {
            if (TextUtils.isEmpty(appKey)) {
                return null;
            }
            return httpRequestUtils.getResponse(getAgentListUrl() + appKey, APPLI_JSON, APPLI_JSON);
        } catch (Exception e) {
            throw new KmException(e.getMessage());
        }
    }

    private String getBotDetailUrl(String botId) {
        return getKmBaseUrl() + BOTS_BASE_URL + botId;
    }

    public String getBotDetail(String botId) {
        String response = httpRequestUtils.getResponse(getBotDetailUrl(botId), APPLI_JSON, APPLI_JSON);
        Utils.printLog(context, TAG, "Bot detail response: " + response);
        return response;
    }

    //gets the kommunicate api user details (with status away/online)
    public String getUserDetails(String userId, String applicationKey) {
        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(applicationKey)) {
            Utils.printLog(context, TAG, "User Id or Application Key is null/empty.");
            return null;
        }

        try {
            String url = getKmBaseUrl() + GET_AGENT_DETAILS + "?applicationId=" + applicationKey.trim() + "&userName=" + URLEncoder.encode(userId, "UTF-8").trim();
            String response = getResponse(url, APPLI_JSON, "application/json, text/plain, */*");
            Utils.printLog(context, TAG, "User details GET method response: " + response);
            return response;
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return null;
    }

    public RegistrationResponse loginKmUser(KMUser user) throws Exception {
        if (user == null) {
            return null;
        }

        user.setDeviceType(Short.valueOf("1"));
        user.setPrefContactAPI(Short.valueOf("2"));
        user.setTimezone(TimeZone.getDefault().getID());
        user.setEnableEncryption(user.isEnableEncryption());
        user.setRoleType(User.RoleType.AGENT.getValue());
        user.setRoleName(User.RoleName.APPLICATION_WEB_ADMIN.getValue());


        MobiComUserPreference mobiComUserPreference = MobiComUserPreference.getInstance(context);

        Gson gson = new Gson();
        user.setAppVersionCode(MOBICOMKIT_VERSION_CODE);
        user.setApplicationId(getApplicationKey(context));
        user.setRegistrationId(mobiComUserPreference.getDeviceRegistrationId());

        if (getAppModuleName(context) != null) {
            user.setAppModuleName(getAppModuleName(context));
        }

        Utils.printLog(context, TAG, "Net status" + Utils.isInternetAvailable(context.getApplicationContext()));

        if (!Utils.isInternetAvailable(context.getApplicationContext())) {
            throw new ConnectException(NO_INTERNET_CONN);
        }

//        Log.i(TAG, "App Id is: " + getApplicationKey(context));
        Utils.printLog(context, TAG, "Registration json " + gson.toJson(user));
        Utils.printLog(context, TAG, "Login url : " + getKmBaseUrl() + USER_LOGIN_API);
        String response = httpRequestUtils.postJsonToServer(getKmBaseUrl() + USER_LOGIN_API, gson.toJson(user));

        Utils.printLog(context, TAG, "Registration response is: " + response);

        if (TextUtils.isEmpty(response) || response.contains("<html")) {
            throw new Exception(SERVICE_UNAVAILABLE);
//            return null;
        }
        if (response.contains(INVALID_APP_ID)) {
            throw new InvalidApplicationException(INVALID_APPLI_ID);
        }
        //final RegistrationResponse registrationResponse = gson.fromJson(response, RegistrationResponse.class);

        if ((new JSONObject(response)).optString("code").equals(INVALID_CREDENTIALS)) {
            throw new UnAuthoriseException(((new JSONObject(response)).optString("message")) + "");
        }

        final KmRegistrationResponse kmRegistrationResponse = gson.fromJson(response, KmRegistrationResponse.class);
        RegistrationResponse registrationResponse = null;
        if (kmRegistrationResponse != null && kmRegistrationResponse.getResult() != null) {
            registrationResponse = kmRegistrationResponse.getResult().getKommunicateUser();
        }

        if (registrationResponse == null) {
            RegistrationResponse invalidResponse = new RegistrationResponse();
            invalidResponse.setMessage(INVALID_RESPONSE);
            return invalidResponse;
        }

        if (registrationResponse.isPasswordInvalid()) {
            throw new UnAuthoriseException(INVALID_USERNAME_PASSWORD);
        }
        Utils.printLog(context, "Registration response ", "is " + registrationResponse);
        if (registrationResponse.getNotificationResponse() != null) {
            Utils.printLog(context, "Registration response ", "" + registrationResponse.getNotificationResponse());
        }
        mobiComUserPreference.setEncryptionKey(registrationResponse.getEncryptionKey());
        mobiComUserPreference.enableEncryption(user.isEnableEncryption());
        mobiComUserPreference.setCountryCode(user.getCountryCode());
        mobiComUserPreference.setUserId(user.getUserId());
        mobiComUserPreference.setContactNumber(user.getContactNumber());
        mobiComUserPreference.setEmailVerified(user.isEmailVerified());
        mobiComUserPreference.setDisplayName(user.getDisplayName());
        mobiComUserPreference.setMqttBrokerUrl(registrationResponse.getBrokerUrl());
        mobiComUserPreference.setDeviceKeyString(registrationResponse.getDeviceKey());
        mobiComUserPreference.setEmailIdValue(user.getEmail());
        mobiComUserPreference.setImageLink(user.getImageLink());
        mobiComUserPreference.setSuUserKeyString(registrationResponse.getUserKey());
        mobiComUserPreference.setLastSyncTimeForMetadataUpdate(String.valueOf(registrationResponse.getCurrentTimeStamp()));
        mobiComUserPreference.setLastSyncTime(String.valueOf(registrationResponse.getCurrentTimeStamp()));
        mobiComUserPreference.setLastSeenAtSyncTime(String.valueOf(registrationResponse.getCurrentTimeStamp()));
        mobiComUserPreference.setChannelSyncTime(String.valueOf(registrationResponse.getCurrentTimeStamp()));
        mobiComUserPreference.setUserBlockSyncTime("10000");
        mobiComUserPreference.setPassword(user.getPassword());
        mobiComUserPreference.setPricingPackage(registrationResponse.getPricingPackage());
        mobiComUserPreference.setAuthenticationType(String.valueOf(user.getAuthenticationTypeId()));
        mobiComUserPreference.setUserRoleType(registrationResponse.getRoleType());
        SettingsSharedPreference.getInstance(context).skipDeletedGroups(user.isSkipDeletedGroups());

        if (user.getUserTypeId() != null) {
            mobiComUserPreference.setUserTypeId(String.valueOf(user.getUserTypeId()));
        }
        if (!TextUtils.isEmpty(user.getNotificationSoundFilePath())) {
            KommunicateSettings.getInstance(context).setCustomNotificationSound(user.getNotificationSoundFilePath());
        }

        Contact contact = new Contact();
        contact.setUserId(user.getUserId());
        contact.setFullName(registrationResponse.getDisplayName());
        contact.setImageURL(registrationResponse.getImageLink());
        contact.setContactNumber(registrationResponse.getContactNumber());
        if (user.getUserTypeId() != null) {
            contact.setUserTypeId(user.getUserTypeId());
        }
        contact.setRoleType(user.getRoleType());
        contact.setMetadata(user.getMetadata());
        contact.setStatus(registrationResponse.getStatusMessage());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            KommunicateSettings.getInstance(context).setNotificationChannelVersion(NotificationChannels.NOTIFICATION_CHANNEL_VERSION - 1);
            new NotificationChannels(context, KommunicateSettings.getInstance(context).getCustomNotificationSound()).prepareNotificationChannels();
        }
        new AppContactService(context).upsert(contact);


        Intent conversationIntentService = new Intent(context, ConversationIntentService.class);
        conversationIntentService.putExtra(ConversationIntentService.SYNC, false);
        ConversationIntentService.enqueueWork(context, conversationIntentService);


        Intent mutedUserListService = new Intent(context, ConversationIntentService.class);
        mutedUserListService.putExtra(ConversationIntentService.MUTED_USER_LIST_SYNC, true);
        ConversationIntentService.enqueueWork(context, mutedUserListService);

        Intent intent = new Intent(context, MqttIntentService.class);
        intent.putExtra(MqttIntentService.CONNECTED_PUBLISH, true);
        MqttIntentService.enqueueWork(context, intent);

        return registrationResponse;
    }

    public String getResponse(String urlString, String contentType, String accept) {
        Utils.printLog(context, TAG, "Calling url: " + urlString);
        boolean isFileUpload = false;

        HttpURLConnection connection = null;

        try {
            connection = createAndGetConnectionObjectForMethodGet(urlString, contentType, accept);
            connection.connect();

            StringBuilder stringBuilder = getResponseForConnection(connection);

            if (!TextUtils.isEmpty(stringBuilder.toString())) {
                if (!TextUtils.isEmpty(MobiComUserPreference.getInstance(context).getEncryptionKey()) && !httpRequestUtils.skipEncryption(urlString)) {
                    return isFileUpload ? stringBuilder.toString() : EncryptionUtils.decrypt(MobiComUserPreference.getInstance(context).getEncryptionKey(), stringBuilder.toString(), MobiComUserPreference.getInstance(context).getEncryptionIV());
                }
            }
            return stringBuilder.toString();
        } catch (ConnectException e) {
            Utils.printLog(context, TAG, "Failed to connect Internet is not working");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            closeConnection(connection);
        }
        return null;
    }

    //uses jwt-token in the authorisation header
    public String getResponseUsingJWTToken(String urlString, String contentType, String accept, String jwtToken) {
        Utils.printLog(context, TAG, "Calling URL(with jwt-token): " + urlString);

        HttpURLConnection connection = null;

        try {
            connection = createAndGetConnectionObjectForMethodGet(urlString, contentType, accept);

            if (!TextUtils.isEmpty(MobiComUserPreference.getInstance(context).getDeviceKeyString())) {
                connection.setRequestProperty(DEVICE_KEY_HEADER, MobiComUserPreference.getInstance(context).getDeviceKeyString());
            }
            if (TextUtils.isEmpty(jwtToken)) {
                Utils.printLog(context, TAG, "The JWT Token is null. Can't set authorization header.");
            } else {
                connection.setRequestProperty(AUTHORIZATION, jwtToken);
            }

            connection.connect();

            return getResponseForConnection(connection).toString();
        } catch (ConnectException exception) {
            Utils.printLog(context, TAG, "Failed to connect. Internet is not working.");
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            closeConnection(connection);
        }
        return null;
    }

    private HttpURLConnection createAndGetConnectionObjectForMethodGet(String urlString, String contentType, String accept) throws Exception {
        URL url = new URL(urlString);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setSSLSocketFactory(SSLPinningConfig.createPinnedSSLSocketFactory());
        connection.setInstanceFollowRedirects(true);
        connection.setRequestMethod(GET);
        connection.setUseCaches(false);
        connection.setDoInput(true);

        if (!TextUtils.isEmpty(contentType)) {
            connection.setRequestProperty(CONTENT_TYPE, contentType);
        }
        if (!TextUtils.isEmpty(accept)) {
            connection.setRequestProperty(ACCEPT, accept);
        }
        return connection;
    }

    private StringBuilder getResponseForConnection(HttpURLConnection connection) throws Exception {
        BufferedReader bufferedReader = null;
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStream inputStream = connection.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, UTF_8));
        } else {
            Utils.printLog(context, TAG, "Response code for getResponse is  :" + connection.getResponseCode());
        }

        StringBuilder stringBuilder = new StringBuilder();
        try {
            String line;
            if (bufferedReader != null) {
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
        Utils.printLog(context, TAG, "Response :" + stringBuilder.toString());
        return stringBuilder;
    }

    private void closeConnection(HttpURLConnection connection) {
        if (connection != null) {
            try {
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
