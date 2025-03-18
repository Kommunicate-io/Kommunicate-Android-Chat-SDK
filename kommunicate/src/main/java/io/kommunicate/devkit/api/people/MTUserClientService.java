package io.kommunicate.devkit.api.people;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import io.kommunicate.devkit.api.HttpRequestUtils;
import io.kommunicate.devkit.api.MobiComKitClientService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/**
 * Created by devashish on 23/12/14.
 */
public class MTUserClientService extends MobiComKitClientService {

    public static final String CHECK_FOR_MT_USER = "/rest/ws/contact/v2/ismtexter";
    private static final String REQUEST_SRC = "?requestSource=1&contactNumber=";
    private static final String appli_path = "application/json";
    private static final String text_path = "text/plain";

    public MTUserClientService(Context context) {
        super(context);
    }

    public String getCheckForMtUser() {
        return getBaseUrl() + CHECK_FOR_MT_USER;
    }

    public ContactContent getContactContent(String contactNumber) {
        String response = null;
        try {
            response = new HttpRequestUtils(context).getResponse(getCheckForMtUser() + REQUEST_SRC + URLEncoder.encode(contactNumber, "UTF-8"), text_path, appli_path);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(response)) {
            Gson gson = new Gson();
            return gson.fromJson(response, ContactContent.class);
        }
        return null;
    }
}
