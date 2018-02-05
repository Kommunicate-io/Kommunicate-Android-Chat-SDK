package io.kommunicate.services;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.applozic.mobicomkit.api.account.user.UserDetail;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.feed.ApiResponse;
import com.applozic.mobicommons.json.GsonUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.kommunicate.users.KmContact;
import io.kommunicate.users.KmUserDetailResponse;
import io.kommunicate.users.KmUserResponse;

/**
 * Created by ashish on 30/01/18.
 */

public class KmUserService {

    private Context context;
    private KmUserClientService userClientService;
    private BaseContactService contactService;

    public KmUserService(Context context) {
        this.context = context;
        userClientService = new KmUserClientService(context);
        contactService = new AppContactService(context);
    }

    public synchronized KmUserResponse getUserList(List<String> roleName, int startIndex, int pageSize) throws Exception {
        ApiResponse response = (ApiResponse) GsonUtils.getObjectFromJson(userClientService.getUserListFilter(roleName, startIndex, pageSize), ApiResponse.class);
        KmUserResponse userResponse = new KmUserResponse();
        List<KmContact> contactList = new ArrayList<>();

        if (response != null && response.isSuccess()) {
            if (response.getResponse() != null) {
                // Map<String, List<String>> userFeedMap = (LinkedTreeMap<String, List<String>>) response.getResponse();

                Type typeToken = new TypeToken<KmUserDetailResponse>() {
                }.getType();

                KmUserDetailResponse responseString = (KmUserDetailResponse) GsonUtils.getObjectFromJson(response.getResponse().toString(), typeToken);
                //Log.d("TestAgent", "Reponse : " + responseString);
                List<UserDetail> userDetailList = responseString.getUsers();

              /*  if (userFeedMap != null && !TextUtils.isEmpty(userFeedMap.get("users"))) {
                    //userDetailList = (UserDetail[]) GsonUtils.getObjectFromJson(userFeedMap.get("users"), UserDetail[].class);
                    userDetailList = (List<UserDetail>) userFeedMap.get("users");
                }*/

                if (userDetailList != null) {
                    for (UserDetail userDetail : userDetailList) {
                        //contactList.add(processUser((UserDetail) GsonUtils.getObjectFromJson(userDetail, UserDetail.class)));
                        contactList.add(processUser(userDetail));
                    }
                    userResponse.setContactList(contactList);
                }
            }
        }

        if (response != null && response.getErrorResponse() != null) {
            userResponse.setErrorList(response.getErrorResponse());
        }

        if (response != null) {
            userResponse.setSuccess(response.isSuccess());
        }

        return userResponse;
    }

    public synchronized KmContact processUser(UserDetail userDetail) {
        KmContact contact = new KmContact();
        contact.setUserId(userDetail.getUserId());
        contact.setContactNumber(userDetail.getPhoneNumber());
        contact.setConnected(userDetail.isConnected());
        contact.setStatus(userDetail.getStatusMessage());
        contact.setFullName(userDetail.getDisplayName());
        contact.setLastSeenAt(userDetail.getLastSeenAtTime());
        contact.setUserTypeId(userDetail.getUserTypeId());
        contact.setUnreadCount(0);
        contact.setLastMessageAtTime(userDetail.getLastMessageAtTime());
        contact.setMetadata(userDetail.getMetadata());
        contact.setRoleType(userDetail.getRoleType());
        if (!TextUtils.isEmpty(userDetail.getImageLink())) {
            contact.setImageURL(userDetail.getImageLink());
        }
        contactService.upsert(contact);
        return contact;
    }
}
