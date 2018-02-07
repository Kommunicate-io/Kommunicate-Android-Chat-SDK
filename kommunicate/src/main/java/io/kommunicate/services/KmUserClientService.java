package io.kommunicate.services;

import android.content.Context;

import com.applozic.mobicomkit.api.HttpRequestUtils;
import com.applozic.mobicomkit.api.account.user.UserClientService;

import java.util.List;

import io.kommunicate.users.KMUser;

/**
 * Created by ashish on 30/01/18.
 */

public class KmUserClientService extends UserClientService {

    private static final String USER_LIST_FILTER_URL = "/rest/ws/user/v3/filter?startIndex=";
    private static final String USER_LOGIN_API = "/login";
    private static final String USER_SIGNUP_API = "/customers?preSignUp=";
    private static final String KM_PROD_BASE_URL = "https://api.kommunicate.io";
    private static final String KM_TEST_BASE_URL = "http://api-test.kommunicate.io";
    private HttpRequestUtils httpRequestUtils;

    public KmUserClientService(Context context) {
        super(context);
        httpRequestUtils = new HttpRequestUtils(context);
    }

    private String getUserListFilterUrl() {
        return getBaseUrl() + USER_LIST_FILTER_URL;
    }

    private String getKmBaseUrl() {
        if (getBaseUrl().contains("apps.applozic")) {
            return KM_PROD_BASE_URL;
        }
        return KM_TEST_BASE_URL;
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

    public String loginKmUser(KMUser user) {
        return null;
    }
}
