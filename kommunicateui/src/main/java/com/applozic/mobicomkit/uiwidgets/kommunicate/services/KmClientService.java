package com.applozic.mobicomkit.uiwidgets.kommunicate.services;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.HttpRequestUtils;
import com.applozic.mobicomkit.api.MobiComKitClientService;

/**
 * Created by ashish on 03/04/18.
 */

public class KmClientService extends MobiComKitClientService {

    private HttpRequestUtils httpRequestUtils;
    public static final String CONVERSATION_SHARE_URL = "https://dashboard.kommunicate.io/conversations/";
    public static final String CONVERSATION_SHARE_TEST_URL = "https://dashboard-test.kommunicate.io/conversations/";
    public static final String CONVERSATION_SHARE_CA_URL = "https://dashboard-ca.kommunicate.io/conversations/";
    public static final String KM_BASE_URL = "https://api.kommunicate.io";
    public static final String KM_TEST_URL = "https://api-test.kommunicate.io";
    public static final String KM_CA_URL = "https://api-ca.kommunicate.io";
    public static final String KM_HELPCENTER_URL = "https://helpcenter.kommunicate.io/?appId=";
    public static final String KM_TEST_HELPCENTER_URL = "https://helpcenter-test.kommunicate.io/?appId=";

    public KmClientService(Context context) {
        super(context);
        httpRequestUtils = new HttpRequestUtils(context);
    }

    public String getConversationShareUrl() {
        if (KM_TEST_URL.equals(getKmBaseUrl())) {
            return CONVERSATION_SHARE_TEST_URL;
        } else if (KM_CA_URL.equals(getKmBaseUrl())) {
            return CONVERSATION_SHARE_CA_URL;
        }
        return CONVERSATION_SHARE_URL;
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

    public String getHelpcenterUrl() {
        String appId = MobiComKitClientService.getApplicationKey(context);
        if (KM_BASE_URL.equals(getKmBaseUrl())) {
            return KM_HELPCENTER_URL + appId;
        } else if (KM_TEST_URL.equals(getKmBaseUrl())) {
            return KM_TEST_HELPCENTER_URL + appId;
        }
        return null;
    }
}
