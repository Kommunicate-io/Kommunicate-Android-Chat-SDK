package com.applozic.mobicomkit.uiwidgets.kommunicate.services;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.HttpRequestUtils;
import com.applozic.mobicomkit.api.MobiComKitClientService;

/**
 * Created by ashish on 03/04/18.
 */

public class KmClientService extends MobiComKitClientService {

    private static final String KM_PROD_BASE_URL = "https://api.kommunicate.io";
    private static final String KM_TEST_BASE_URL = "https://api-test.kommunicate.io";
    private HttpRequestUtils httpRequestUtils;

    public KmClientService(Context context) {
        super(context);
        httpRequestUtils = new HttpRequestUtils(context);
    }

    private String getKmBaseUrl() {
        if (getBaseUrl().contains("apps.applozic")) {
            return KM_PROD_BASE_URL;
        }
        return KM_TEST_BASE_URL;
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
}
