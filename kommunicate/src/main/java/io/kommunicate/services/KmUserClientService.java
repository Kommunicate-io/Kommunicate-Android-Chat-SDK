package io.kommunicate.services;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.HttpRequestUtils;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.api.account.user.UserClientService;
import com.applozic.mobicomkit.api.conversation.ApplozicMqttIntentService;
import com.applozic.mobicomkit.api.conversation.ConversationIntentService;
import com.applozic.mobicomkit.api.notification.NotificationChannels;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.exception.InvalidApplicationException;
import com.applozic.mobicomkit.exception.UnAuthoriseException;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.encryption.EncryptionUtils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.contact.Contact;
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

import io.kommunicate.KMGroupInfo;
import io.kommunicate.KmException;
import io.kommunicate.feeds.KmRegistrationResponse;
import io.kommunicate.users.KMUser;

/**
 * Created by ashish on 30/01/18.
 */

public class KmUserClientService extends UserClientService {

    private static final String USER_LIST_FILTER_URL = "/rest/ws/user/v3/filter?startIndex=";
    private static final String USER_LOGIN_API = "/login";
    private static final String GET_APPLICATION_LIST = "/rest/ws/user/getlist";
    private static final String CONVERSATION_URL = "/conversations";
    private static final String KM_GET_HELPDOCS_KEY_URL = "/integration/settings/";
    private static final String KM_HELPDOCS_URL = "https://api.helpdocs.io/v1/article";
    private static final String KM_HELPDOCS_SERACH_URL = "https://api.helpdocs.io/v1/search?key=";
    private static final String USER_PASSWORD_RESET = "/users/password-reset";
    private static final String INVALID_APP_ID = "INVALID_APPLICATIONID";
    private static final String CREATE_CONVERSATION_URL = "/create";
    private static final String GET_AGENT_LIST_URL = "/users/chat/plugin/settings";
    public HttpRequestUtils httpRequestUtils;
    private static String TAG = "KmUserClientService";

    public KmUserClientService(Context context) {
        super(context);
        httpRequestUtils = new HttpRequestUtils(context);
    }

    private String getUserListFilterUrl() {
        return getBaseUrl() + USER_LIST_FILTER_URL;
    }

    private String getArticleListUrl() {
        return KM_HELPDOCS_URL + "?key=";
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
        return getKmBaseUrl() + GET_AGENT_LIST_URL;
    }

    private String getApplicationListUrl() {
        return getBaseUrl() + GET_APPLICATION_LIST;
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
            jsonObject.put("participantUserId", userId);
            jsonObject.put("createdBy", userId);
            jsonObject.put("defaultAgentId", agentId);
            jsonObject.put("applicationId", applicationId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            String response = httpRequestUtils.postData(getConversationUrl(), "application/json", "application/json", jsonObject.toString());
            Utils.printLog(context, TAG, "Response : " + response);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String createConversation(KMGroupInfo channelInfo) throws Exception {
        if (channelInfo == null) {
            throw new KmException("ChannelInfo cannot be null");
        }

        String channelJson = GsonUtils.getJsonFromObject(channelInfo, KMGroupInfo.class);
        return httpRequestUtils.postData(getCreateConversationUrl(), "application/json", "application/json", channelJson);
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

    public String getApplicationList(String userId, boolean isEmailId) {
        if (TextUtils.isEmpty(userId)) {
            return null;
        }
        try {
            String url = getApplicationListUrl() + "?roleNameList=APPLICATION_WEB_ADMIN&" + (isEmailId ? "emailId=" : "userId=") + URLEncoder.encode(userId, "UTF-8");
            return httpRequestUtils.getResponse(url, "application/json", "application/json");
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

            String url = getAgentListUrl() + "?appId=" + appKey;
            return httpRequestUtils.getResponse(url, "application/json", "application/json");
        } catch (Exception e) {
            throw new KmException(e.getMessage());
        }
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
            throw new ConnectException("No Internet Connection");
        }

//        Log.i(TAG, "App Id is: " + getApplicationKey(context));
        Utils.printLog(context, TAG, "Registration json " + gson.toJson(user));
        Utils.printLog(context, TAG, "Login url : " + getKmBaseUrl() + USER_LOGIN_API);
        String response = httpRequestUtils.postJsonToServer(getKmBaseUrl() + USER_LOGIN_API, gson.toJson(user));

        Utils.printLog(context, TAG, "Registration response is: " + response);

        if (TextUtils.isEmpty(response) || response.contains("<html")) {
            throw new Exception("503 Service Unavailable");
//            return null;
        }
        if (response.contains(INVALID_APP_ID)) {
            throw new InvalidApplicationException("Invalid Application Id");
        }
        //final RegistrationResponse registrationResponse = gson.fromJson(response, RegistrationResponse.class);

        if ((new JSONObject(response)).optString("code").equals("INVALID_CREDENTIALS")) {
            throw new UnAuthoriseException(((new JSONObject(response)).optString("message")) + "");
        }

        final KmRegistrationResponse kmRegistrationResponse = gson.fromJson(response, KmRegistrationResponse.class);
        RegistrationResponse registrationResponse = null;
        if (kmRegistrationResponse != null && kmRegistrationResponse.getResult() != null) {
            registrationResponse = kmRegistrationResponse.getResult().getApplozicUser();
        }

        if (registrationResponse == null) {
            RegistrationResponse invalidResponse = new RegistrationResponse();
            invalidResponse.setMessage("Invalid response");
            return invalidResponse;
        }

        if (registrationResponse.isPasswordInvalid()) {
            throw new UnAuthoriseException("Invalid uername/password");
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
        ApplozicClient.getInstance(context).skipDeletedGroups(user.isSkipDeletedGroups());

        if (user.getUserTypeId() != null) {
            mobiComUserPreference.setUserTypeId(String.valueOf(user.getUserTypeId()));
        }
        if (!TextUtils.isEmpty(user.getNotificationSoundFilePath())) {
            Applozic.getInstance(context).setCustomNotificationSound(user.getNotificationSoundFilePath());
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
            Applozic.getInstance(context).setNotificationChannelVersion(NotificationChannels.NOTIFICATION_CHANNEL_VERSION - 1);
            new NotificationChannels(context, Applozic.getInstance(context).getCustomNotificationSound()).prepareNotificationChannels();
        }
        contact.processContactNumbers(context);
        new AppContactService(context).upsert(contact);


        Intent conversationIntentService = new Intent(context, ConversationIntentService.class);
        conversationIntentService.putExtra(ConversationIntentService.SYNC, false);
        ConversationIntentService.enqueueWork(context, conversationIntentService);


        Intent mutedUserListService = new Intent(context, ConversationIntentService.class);
        mutedUserListService.putExtra(ConversationIntentService.MUTED_USER_LIST_SYNC, true);
        ConversationIntentService.enqueueWork(context, mutedUserListService);

        Intent intent = new Intent(context, ApplozicMqttIntentService.class);
        intent.putExtra(ApplozicMqttIntentService.CONNECTED_PUBLISH, true);
        ApplozicMqttIntentService.enqueueWork(context, intent);

        return registrationResponse;
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
}
