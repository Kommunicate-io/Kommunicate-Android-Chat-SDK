package io.kommunicate;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicommons.ApplozicService;

//can and should be a sub-class of MobiComUserPreference
//however inheritance isn't allowed in that class
//TODO: won't be needed after applozic sdk update
public class KmUserPreference {
    private static String JWT_TOKEN = "jwt-token";

    private static KmUserPreference agentSharedPreference;
    private static SharedPreferences sharedPreferences;

    private KmUserPreference(Context context) {
        context = ApplozicService.getContext(context);
        ApplozicService.initWithContext(context);
        if (!TextUtils.isEmpty(MobiComKitClientService.getApplicationKey(ApplozicService.getContext(context)))) {
            sharedPreferences = context.getSharedPreferences(MobiComKitClientService.getApplicationKey(ApplozicService.getContext(context)), Context.MODE_PRIVATE);
        }
    }

    public static KmUserPreference getInstance(Context context) {
        if (agentSharedPreference == null) {
            agentSharedPreference = new KmUserPreference(ApplozicService.getContext(context));
        } else {
            if (!TextUtils.isEmpty(MobiComKitClientService.getApplicationKey(ApplozicService.getContext(context)))) {
                sharedPreferences = ApplozicService.getContext(context).getSharedPreferences(MobiComKitClientService.getApplicationKey(ApplozicService.getContext(context)), Context.MODE_PRIVATE);
            }
        }
        return agentSharedPreference;
    }

    public void setJwtToken(String jwtToken) {
        sharedPreferences.edit().putString(JWT_TOKEN, jwtToken).commit();
    }

    public String getJwtToken() {
        return sharedPreferences.getString(JWT_TOKEN, null);
    }
}
