package io.kommunicate.zendesk;

import org.json.JSONException;
import org.json.JSONObject;

public class KmZendeskUtils {

    public static JSONObject createMessageProxy(String displayName, String agentId, Integer conversationId, Long messageTimestamp) throws JSONException {
        JSONObject messageProxy = new JSONObject();
        JSONObject agentInfo = new JSONObject();
        agentInfo.put("displayName", displayName);
        agentInfo.put("agentId", agentId);
        messageProxy.put("groupId", conversationId);
        messageProxy.put("fromUserName", agentId);
        messageProxy.put("messageDeduplicationKey", agentId + "-" + messageTimestamp);
        messageProxy.put("agentInfo", agentInfo);
        return messageProxy;
    }
}
