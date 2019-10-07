package io.kommunicate.services;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.HttpRequestUtils;
import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicommons.commons.core.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    public static final String KM_AUTO_SUGGESTION_URL = "/autosuggest/message/";
    public static final String KM_AUTO_SUGGESTION_ENDPOINT = "?type=shortcut";
    public static final String KM_DASHBOARD = "km_dashboard_url";
    public static final String KM_HELPCENTER = "km_helpcenter_url";
    private static final String CONVERSATION_FEEDBACK_URL = "/feedback";

    private static final String TAG = "KmClientService";

    public KmClientService(Context context) {
        super(context);
        httpRequestUtils = new HttpRequestUtils(context);
    }

    private String getFeedbackUrl() {
        return getKmBaseUrl() + CONVERSATION_FEEDBACK_URL;
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

    public String getKmAutoSuggestions() {
        return httpRequestUtils.getResponse(getKmAutoSuggestionUrl() + getApplicationKey(context) + KM_AUTO_SUGGESTION_ENDPOINT, "application/json", "application/json");
    }

    public String getHelpCenterUrl() {
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
    public String postConversationFeedback(int conversationId, int rating, String feedbackComment[]) throws Exception {
        JSONObject jsonObject = new JSONObject();
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
        } catch (JSONException j) {
            j.printStackTrace();
        }

        try {
            String response = httpRequestUtils.postData(getFeedbackUrl(), "application/json", "application/json", jsonObject.toString());

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
        StringBuilder urlBuilder = new StringBuilder(getFeedbackUrl());
        if (!TextUtils.isEmpty(conversationId)) {
            urlBuilder.append("/");
            urlBuilder.append(conversationId);
        }

        String response = httpRequestUtils.getResponse(urlBuilder.toString(), "application/json", "application/json");

        Utils.printLog(context, TAG, "Get feedback response: " + response);

        return httpRequestUtils.getResponse(urlBuilder.toString(), "application/json", "application/json");
    }
}
