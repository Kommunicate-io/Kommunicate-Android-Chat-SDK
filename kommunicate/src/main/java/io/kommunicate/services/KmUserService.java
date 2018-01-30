package io.kommunicate.services;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.account.user.UserDetail;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.feed.ApiResponse;
import com.applozic.mobicommons.json.GsonUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

import io.kommunicate.KmContact;
import io.kommunicate.KmUserResponse;

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

    public synchronized KmUserResponse getUserList(List<String> roleName, int startIndex, int pageSize) {
        ApiResponse response = (ApiResponse) GsonUtils.getObjectFromJson(userClientService.getUserListFilter(roleName, startIndex, pageSize), ApiResponse.class);
        KmUserResponse userResponse = new KmUserResponse();
        List<KmContact> contactList = new ArrayList<>();

        if (response != null && response.isSuccess()) {
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = null;

            if (response.getResponse() != null) {
                jsonObject = jsonParser.parse(response.getResponse().toString()).getAsJsonObject();
            }
            if (jsonObject == null) {
                return null;
            }

            UserDetail[] userDetailList = (UserDetail[]) GsonUtils.getObjectFromJson(jsonObject.get("users").toString(), UserDetail[].class);

            for (UserDetail userDetail : userDetailList) {
                contactList.add(processUser(userDetail));
            }
            userResponse.setContactList(contactList);
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
