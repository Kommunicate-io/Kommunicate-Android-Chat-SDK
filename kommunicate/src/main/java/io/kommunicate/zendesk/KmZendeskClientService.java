package io.kommunicate.zendesk;

import android.content.Context;
import com.applozic.mobicomkit.api.HttpRequestUtils;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import io.kommunicate.services.KmClientService;
import zendesk.chat.Attachment;
import zendesk.chat.JwtAuthenticator;

public class KmZendeskClientService extends KmClientService {
    private HttpRequestUtils httpRequestUtils;
    private static final String KM_ZENDESK_JWT_URL = "/rest/ws/zendesk/jwt";
    private static final String KM_ZENDESK_SEND_MESSAGE = "/rest/ws/zendesk/message/send";
    private static final String KM_ZENDESK_SEND_ATTACHMENT = "/rest/ws/zendesk/file/send";
    private static final String TAG = "KmZendeskClientService";

    public KmZendeskClientService(Context context) {
        super(context);
        httpRequestUtils = new HttpRequestUtils(context);
    }

    private String getKmZendeskJwtUrl() {
        return getKmBaseUrl() + KM_ZENDESK_JWT_URL;
    }
    private String getKmZendeskSendMessageUrl() { return getKmBaseUrl() + KM_ZENDESK_SEND_MESSAGE; }
    private String getKmZendeskSendAttachmentUrl() { return getKmBaseUrl() + KM_ZENDESK_SEND_ATTACHMENT; }

    public KmZendeskApiModel sendZendeskMessage(String message, String displayName, String agentId, Integer conversationId, Long messageTimestamp) {
        try {
            JSONObject messageProxy = new JSONObject();
            JSONObject agentInfo = new JSONObject();
            agentInfo.put("displayName", displayName);
            agentInfo.put("agentId", agentId);
                    messageProxy.put("message", message);
                    messageProxy.put("groupId", conversationId);
                    messageProxy.put("fromUserName", agentId);
                    messageProxy.put("messageDeduplicationKey", agentId + "-" + messageTimestamp);
                    messageProxy.put("agentInfo", agentInfo);


            String response = httpRequestUtils.postData(getKmZendeskSendMessageUrl(), "application/json", "application/json", messageProxy.toString());
            KmZendeskApiModel jwtResponse = (KmZendeskApiModel) GsonUtils.getObjectFromJson(response,
                    KmZendeskApiModel.class);
            Utils.printLog(context, TAG, "Zendesk Send Agent Message response : " + response);
            return jwtResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public KmZendeskApiModel sendZendeskAttachment(Attachment attachment, String displayName, String agentId, Integer conversationId, Long messageTimestamp) {
        try {
            JSONObject messageProxy = new JSONObject();
            JSONObject agentInfo = new JSONObject();
            agentInfo.put("displayName", displayName);
            agentInfo.put("agentId", agentId);
            messageProxy.put("auth", MobiComUserPreference.getInstance(context).getUserAuthToken());
            String fileAttachment = GsonUtils.getJsonFromObject(attachment, Attachment.class);
            messageProxy.put("fileAttachment", new JSONObject(fileAttachment.replaceAll("mimeType", "mime_type")));
            messageProxy.put("groupId", conversationId);
            messageProxy.put("fromUserName", agentId);
            messageProxy.put("messageDeduplicationKey", agentId + "-" + messageTimestamp);
            messageProxy.put("agentInfo", agentInfo);
            String response = httpRequestUtils.postData(getKmZendeskSendAttachmentUrl(), "application/json", "application/json", messageProxy.toString());
            KmZendeskApiModel jwtResponse = (KmZendeskApiModel) GsonUtils.getObjectFromJson(response,
                    KmZendeskApiModel.class);
            Utils.printLog(context, TAG, "Zendesk Send Agent Attachment response : " + response);
            return jwtResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void getJwtForZendeskAuthentication(String userId, String name, String emailId, JwtAuthenticator.JwtCompletion callback) throws Exception {
        JSONObject userObject = new JSONObject();
        try {
            userObject.put("name", name);
            userObject.put("email", emailId);
            userObject.put("externalId", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            String response = httpRequestUtils.postData(getKmZendeskJwtUrl(), "application/json", "application/json", userObject.toString());
            KmZendeskApiModel jwtResponse = (KmZendeskApiModel) GsonUtils.getObjectFromJson(response,
                    KmZendeskApiModel.class);
            callback.onTokenLoaded(jwtResponse.getData().getJwt());

            Utils.printLog(context, TAG, "Zendesk JWT response : " + response);
        } catch (Exception e) {
            throw e;
        }
    }
}
