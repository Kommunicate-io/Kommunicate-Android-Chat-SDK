package com.applozic.mobicomkit.uiwidgets.kommunicate.services;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.HttpRequestUtils;
import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.uiwidgets.R;

import java.util.Arrays;
import java.util.List;

/**
 * Created by ashish on 03/04/18.
 */

public class KmClientService extends MobiComKitClientService {

    private HttpRequestUtils httpRequestUtils;
    public static final String CONVERSATION_SHARE_ENDPOINT = "/conversations/";
    public static final String HELCENTER_APPID_ENDPOINT = "/?appId=";
    public static final String KM_DASHBOARD = "km_dashboard_url";
    public static final String KM_HELPCENTER = "km_helpcenter_url";

    public KmClientService(Context context) {
        super(context);
        httpRequestUtils = new HttpRequestUtils(context);
    }

    public String getConversationShareUrl() {
        return getKmMappedUrl(KM_DASHBOARD) + CONVERSATION_SHARE_ENDPOINT;
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
}
