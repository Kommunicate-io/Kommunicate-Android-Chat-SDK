package io.kommunicate.services;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.account.user.UserDetail;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.feed.ApiResponse;
import com.applozic.mobicomkit.feed.ChannelFeed;
import com.applozic.mobicomkit.feed.ChannelFeedApiResponse;
import com.applozic.mobicommons.json.GsonUtils;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.kommunicate.KMGroupInfo;
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

    public synchronized KmUserResponse getUserList(List<String> roleName, int startIndex, int pageSize, int orderBy) throws Exception {
        KmUserResponse userResponse = new KmUserResponse();
        try {
            ApiResponse response = (ApiResponse) GsonUtils.getObjectFromJson(userClientService.getUserListFilter(roleName, startIndex, pageSize, orderBy), ApiResponse.class);
            List<KmContact> contactList = new ArrayList<>();

            if (response != null && response.isSuccess()) {
                if (response.getResponse() != null) {

                    Type typeToken = new TypeToken<KmUserDetailResponse>() {
                    }.getType();

                    KmUserDetailResponse responseString = (KmUserDetailResponse) GsonUtils.getObjectFromJson(GsonUtils.getJsonFromObject(response.getResponse(), Object.class), typeToken);
                    List<UserDetail> userDetailList = responseString.getUsers();

                    if (userDetailList != null) {
                        for (UserDetail userDetail : userDetailList) {
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
        } catch (Exception e) {
            e.printStackTrace();
            userResponse.setException(e);
        }
        return userResponse;
    }

    public synchronized String getBotDetailResponse(String botId) {
        if(TextUtils.isEmpty(botId)) {
            return null;
        }
        return userClientService.getBotDetail(botId);
    }

    public synchronized String createConversation(Integer groupId, String userId, String agentId, String applicationId) {
        return userClientService.createConversation(groupId, userId, agentId, applicationId);
    }

    public synchronized String createNewConversation(KMGroupInfo channelInfo) throws Exception {
        String response = userClientService.createConversation(channelInfo);
        if (response != null) {
            ChannelFeedApiResponse apiResponse = (ChannelFeedApiResponse) GsonUtils.getObjectFromJson(response, ChannelFeedApiResponse.class);

            if (apiResponse != null && apiResponse.isSuccess() && apiResponse.getResponse() != null) {
                ChannelFeed[] channelFeeds = new ChannelFeed[1];
                channelFeeds[0] = apiResponse.getResponse();
                ChannelService.getInstance(context).processChannelFeedList(channelFeeds, true);
            }
        }
        return response;
    }

    public synchronized Map<String, String> getApplicationList(String userId, boolean isEmailId) {
        try {
            return (Map<String, String>) GsonUtils.getObjectFromJson(userClientService.getApplicationList(userId, isEmailId), Map.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized String getHelpDocsKey(String appKey, String type) throws Exception {
        return userClientService.getHelpDocsKey(appKey, type);
    }

    public synchronized String getArticleList(String helpDocsKey) throws Exception {
        return userClientService.getArticleList(helpDocsKey);
    }

    public String getSelectedArticles(String helpDocsKey, String queryString) throws Exception {
        return userClientService.getSelectedArticles(helpDocsKey, queryString);
    }

    public String getArticleAnswer(String helpDocsKey, String articleId) throws Exception {
        return userClientService.getArticleAnswer(articleId, helpDocsKey);
    }

    public String getDashboardFaq(String appKey, String articleId) throws Exception {
        return userClientService.getDashboardFaq(appKey, articleId);
    }

    public synchronized String getAgentList(String appKey) throws Exception {
        return userClientService.getAgentList(appKey);
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
        contact.setDeletedAtTime(userDetail.getDeletedAtTime());
        if (!TextUtils.isEmpty(userDetail.getImageLink())) {
            contact.setImageURL(userDetail.getImageLink());
        }
        contactService.upsert(contact);
        return contact;
    }
}
