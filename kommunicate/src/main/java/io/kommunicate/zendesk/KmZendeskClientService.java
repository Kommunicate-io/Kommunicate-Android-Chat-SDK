package io.kommunicate.zendesk;

import android.content.Context;
import android.util.Log;

import com.applozic.mobicomkit.api.HttpRequestUtils;
import com.applozic.mobicomkit.feed.ApiResponse;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import io.kommunicate.services.KmClientService;
import zendesk.chat.JwtAuthenticator;

public class KmZendeskClientService extends KmClientService {
    private HttpRequestUtils httpRequestUtils;
    private static final String KM_ZENDESK_JWT_URL = "/rest/ws/zendesk/jwt";
    private static final String TAG = "KmZendeskClientService";

    public KmZendeskClientService(Context context) {
        super(context);
        httpRequestUtils = new HttpRequestUtils(context);
    }

    private String getKmZendeskJwtUrl() {
        return getKmBaseUrl() + KM_ZENDESK_JWT_URL;
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
        Log.e("zendeskuser", userObject.toString());
        try {
            String response = httpRequestUtils.postData(getKmZendeskJwtUrl(), "application/json", "application/json", userObject.toString());
            KmZendeskJwtModel jwtResponse = (KmZendeskJwtModel) GsonUtils.getObjectFromJson(response,
                    KmZendeskJwtModel.class);
            callback.onTokenLoaded(jwtResponse.getData().getJwt());

            Utils.printLog(context, TAG, "Zendesk JWT response : " + response);
            Log.e("zendeskuser", response);

            //return response;
        } catch (Exception e) {
            throw e;
        }
    }
}
