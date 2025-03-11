package dev.kommunicate.devkit.api.conversation.stat;

import android.content.Context;

import dev.kommunicate.devkit.api.HttpRequestUtils;
import dev.kommunicate.devkit.api.MobiComKitClientService;

import dev.kommunicate.commons.json.GsonUtils;

/**
 * Created by devashish on 26/12/14.
 */
public class MessageStatClientService extends MobiComKitClientService {

    public static final String MESSAGE_STAT_URL = "/rest/ws/sms/stat/update";
    private static final String TAG = "MessageStatClientService";
    private static final String application_json = "application/json";

    public MessageStatClientService(Context context) {
        super(context);
    }

    public String getMessageStatUrl() {
        return getBaseUrl() + MESSAGE_STAT_URL;
    }


    public String sendMessageStat(MessageStat messageStat) {
        try {
            return new HttpRequestUtils(context).postData(getMessageStatUrl(), application_json, null, GsonUtils.getJsonFromObject(messageStat, MessageStat.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
