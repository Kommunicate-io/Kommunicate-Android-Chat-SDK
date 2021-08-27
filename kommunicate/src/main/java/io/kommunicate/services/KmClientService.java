package io.kommunicate.services;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.api.HttpRequestUtils;
import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicommons.commons.core.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import io.kommunicate.R;

/**
 * Created by ashish on 03/04/18.
 * updated by shubham on 07/19. (feedback)
 */

public class KmClientService extends MobiComKitClientService {

    private HttpRequestUtils httpRequestUtils;
    public static final String CONVERSATION_SHARE_ENDPOINT = "/conversations/";
    public static final String HELCENTER_APPID_ENDPOINT = "/?appId=";
    public static final String FAQ_PAGE_ENDPOINT = "/category/";
    public static final String KM_AUTO_SUGGESTION_URL = "/autosuggest/message/";
    public static final String KM_AUTO_SUGGESTION_ENDPOINT = "?type=shortcut";
    public static final String KM_DASHBOARD = "km_dashboard_url";
    public static final String KM_HELPCENTER = "km_helpcenter_url";
    private static final String CONVERSATION_FEEDBACK_URL = "/feedback";
    public static final String APP_SETTING_URL = "/users/v2/chat/plugin/settings?appId=";
    private static final String CHANGE_CONVERSATION_ASSIGNEE_URL = "/rest/ws/group/assignee/change?groupId=";

    private static final String TAG = "KmClientService";
    private String faqPageName = null;
    public KmClientService(Context context) {
        super(context);
        httpRequestUtils = new HttpRequestUtils(context);
    }
    public KmClientService(Context context,String faqPageName) {
        super(context);
        httpRequestUtils = new HttpRequestUtils(context);
        this.faqPageName = faqPageName;
    }

    private String getFeedbackGetUrl() {
        return getKmBaseUrl() + CONVERSATION_FEEDBACK_URL;
    }

    private String getFeedbackPostUrl() {
        return getKmBaseUrl() + CONVERSATION_FEEDBACK_URL + "/v2?sendAsMessage=true";
    }

    public String getConversationShareUrl() {
        return getKmMappedUrl(KM_DASHBOARD) + CONVERSATION_SHARE_ENDPOINT;
    }

    private String getKmAutoSuggestionUrl() {
        return getKmBaseUrl() + KM_AUTO_SUGGESTION_URL;
    }

    private String getAwayMessageUrl() {
        return getKmBaseUrl() + "/applications/";
    }

    private String getAppSettingUrl() {
        return getKmBaseUrl() + APP_SETTING_URL;
    }

    public String getAwayMessage(String appKey, Integer groupId) throws Exception {
        StringBuilder urlBuilder = new StringBuilder(getAwayMessageUrl());
        if (!TextUtils.isEmpty(appKey)) {
            urlBuilder.append(appKey);
            urlBuilder.append("/awaymessage?conversationId=");
        }
        if (groupId != null && !groupId.equals(0)) {
            urlBuilder.append(groupId);
        }

        return httpRequestUtils.getResponse(urlBuilder.toString(), "application/json", "application/json");
    }

    public String switchConversationAssignee(Integer groupId, String assigneeId, boolean switchAssignee, boolean sendNotifyMessage, boolean takeOverFromBot) {
        try {
            String url = getBaseUrl() + CHANGE_CONVERSATION_ASSIGNEE_URL + groupId
                    + "&assignee=" + URLEncoder.encode(assigneeId, "UTF-8").trim()
                    + "&switchAssignee=" + switchAssignee
                    + "&sendNotifyMessage=" + sendNotifyMessage
                    + "&takeOverFromBot=" + takeOverFromBot;

            return httpRequestUtils.makePatchRequest(url, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getKmAutoSuggestions() {
        return httpRequestUtils.getResponse(getKmAutoSuggestionUrl() + getApplicationKey(context) + KM_AUTO_SUGGESTION_ENDPOINT, "application/json", "application/json");
    }

    public String getAppSetting(String appId) {
        return httpRequestUtils.getResponse(getAppSettingUrl() + appId, "application/json", "application/json");
    }

    public String getHelpCenterUrl() {
        if(faqPageName!=null)
            return getKmMappedUrl(KM_HELPCENTER) + FAQ_PAGE_ENDPOINT + faqPageName+ HELCENTER_APPID_ENDPOINT + MobiComKitClientService.getApplicationKey(context);
        else
            return getKmMappedUrl(KM_HELPCENTER) + HELCENTER_APPID_ENDPOINT + MobiComKitClientService.getApplicationKey(context);
    }

    public String getKmMappedUrl(String urlMapper) {
        if (TextUtils.isEmpty(urlMapper) || TextUtils.isEmpty(getKmBaseUrl())) {
            return null;
        }
        List<String> baseUrlList = Arrays.asList(context.getResources().getStringArray(R.array.km_base_url));

        if (baseUrlList.size() == 0) {
            return null;
        }

        return context.getResources()
                .getStringArray(context.getResources()
                        .getIdentifier(urlMapper, "array", context.getPackageName()))[baseUrlList.indexOf(getKmBaseUrl())];
    }

    /**
     * to post the feedback for a given conversation
     *
     * @param conversationId  the groupId of the conversation
     * @param rating          the rating 0-5 given by the user
     * @param feedbackComment the comment array of the inputs given by the user
     * @return the feedback response json string
     */
    public String postConversationFeedback(int conversationId, int rating, String[] feedbackComment, String userName, String userId, String supportAgentId) throws Exception {
        JSONObject jsonObject = new JSONObject();
        JSONObject feedbackUserJson = new JSONObject();
        JSONArray feedbackJsonArray = new JSONArray();

        if (feedbackComment != null) {
            for (String feedback : feedbackComment) {
                feedbackJsonArray.put(feedback);
            }
        }

        try {
            jsonObject.put("groupId", conversationId);
            if (feedbackComment != null) {
                if (feedbackComment.length > 0) {
                    jsonObject.put("comments", feedbackJsonArray);
                }
            }
            jsonObject.put("rating", rating);
            jsonObject.put("applicationId", Applozic.getInstance(context).getApplicationKey());
            jsonObject.put("supportAgentName", supportAgentId); //not a mistake or typo
            feedbackUserJson.put("name", userName);
            feedbackUserJson.put("userId", userId);
            jsonObject.put("userInfo", feedbackUserJson);
        } catch (JSONException j) {
            j.printStackTrace();
        }

        try {
            String response = httpRequestUtils.postData(getFeedbackPostUrl(), "application/json", "application/json", jsonObject.toString());

            Utils.printLog(context, TAG, "Post feedback response : " + response);

            return response;
        } catch (Exception e) {
            throw e;
        }
    }


    /**
     * to get the feedback of given conversation
     *
     * @param conversationId the groupId of the conversation to get the feedback of
     * @return the response (feedback json)
     */
    public String getConversationFeedback(String conversationId) {
        StringBuilder urlBuilder = new StringBuilder(getFeedbackGetUrl());
        if (!TextUtils.isEmpty(conversationId)) {
            urlBuilder.append("/");
            urlBuilder.append(conversationId);
        }

        String response = httpRequestUtils.getResponse(urlBuilder.toString(), "application/json", "application/json");

        Utils.printLog(context, TAG, "Get feedback response: " + response);

        return response;
    }
}
