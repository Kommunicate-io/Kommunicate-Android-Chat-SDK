package com.applozic.mobicomkit.channel.service;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.MultipleChannelFeedApiResponse;
import com.applozic.mobicomkit.api.HttpRequestUtils;
import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.api.notification.MuteNotificationRequest;
import com.applozic.mobicomkit.api.people.ChannelInfo;
import com.applozic.mobicomkit.feed.ApiResponse;
import com.applozic.mobicomkit.feed.ChannelFeed;
import com.applozic.mobicomkit.feed.ChannelFeedApiResponse;
import com.applozic.mobicomkit.feed.ChannelFeedListResponse;
import com.applozic.mobicomkit.feed.GroupInfoUpdate;
import com.applozic.mobicomkit.sync.SyncChannelFeed;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.List;
import java.util.Set;

/**
 * Created by sunil on 29/12/15.
 */
public class ChannelClientService extends MobiComKitClientService {
    private static final String CHANNEL_INFO_URL = "/rest/ws/group/info";
    // private static final String CHANNEL_SYNC_URL = "/rest/ws/group/list";
    private static final String CHANNEL_SYNC_URL = "/rest/ws/group/v3/list";
    private static final String CREATE_CHANNEL_URL = "/rest/ws/group/create";
    private static final String CREATE_MULTIPLE_CHANNEL_URL = "/rest/ws/group/create/multiple";
    private static final String ADD_MEMBER_TO_CHANNEL_URL = "/rest/ws/group/add/member";
    private static final String REMOVE_MEMBER_FROM_CHANNEL_URL = "/rest/ws/group/remove/member";
    private static final String CHANNEL_UPDATE_URL = "/rest/ws/group/update";
    private static final String CHANNEL_LEFT_URL = "/rest/ws/group/left";
    private static final String ADD_MEMBER_TO_MULTIPLE_CHANNELS_URL = "/rest/ws/group/add/user";
    private static final String CHANNEL_DELETE_URL = "/rest/ws/group/delete";
    private static final String REMOVE_MEMBERS_FROM_MULTIPE_CHANNELS = "/rest/ws/group/remove/user";
    private static final String MUTE_CHANNEL_UPDATE = "/rest/ws/group/user/update";
    private static final String ADD_MEMBERS_TO_CONTACT_GROUP_URL = "/rest/ws/group/%s/add";
    private static final String GET_MEMBERS_FROM_CONTACT_GROUP_URL = "/rest/ws/group/%s/get";
    private static final String GET_GROUP_INFO_FROM_GROUP_IDS_URL = "/rest/ws/group/details";
    private static final String ADD_MEMBERS_TO_CONTACT_GROUP_OF_TYPE_URL =
            "/rest/ws/group/%s/add/members";
    private static final String GET_MEMBERS_TO_CONTACT_GROUP_OF_TYPE_URL = "/rest/ws/group/%s/get";
    private static final String GET_MEMBERS_FROM_CONTACT_GROUP_LIST_URL =
            "/rest/ws/group/favourite/list/get";
    private static final String CREATE_CONVERSATION_URL = "/conversations";
    private static final String UPDATED_AT = "updatedAt";
    private static final String USER_ID = "userId";
    private static final String GROUP_ID = "groupId";
    private static final String UPDATE_CLIENT_GROUP_ID = "updateClientGroupId";
    private static final String RESET_UNREAD_COUNT = "resetCount";
    private static final String CLIENT_GROUPID = "clientGroupId";
    private static final String GROUPIDS = "groupIds";
    private static final String GROUP_NAME = "groupName";
    private static final String CLIENT_GROUPIDs = "clientGroupIds";
    private static final String GROUPTYPE = "groupType";
    private static final String TAG = "ChannelClientService";
    private static ChannelClientService channelClientService;
    private static final String REMOVE_MEMBERS_FROM_CONTACT_GROUP_OF_TYPE_URL =
            "/rest/ws/group/%s/remove";
    private HttpRequestUtils httpRequestUtils;

    private ChannelClientService(Context context) {
        super(context);
        this.context = ApplozicService.getContext(context);
        this.httpRequestUtils = new HttpRequestUtils(context);
    }


    public static ChannelClientService getInstance(Context context) {
        if (channelClientService == null) {
            channelClientService = new ChannelClientService(ApplozicService.getContext(context));
        }
        return channelClientService;
    }

    public String getChannelInfoUrl() {
        return getBaseUrl() + CHANNEL_INFO_URL;
    }

    public String getChannelSyncUrl() {
        return getBaseUrl() + CHANNEL_SYNC_URL;
    }

    public String getCreateChannelUrl() {
        return getBaseUrl() + CREATE_CHANNEL_URL;
    }

    public String getMuteChannelUrl() {
        return getBaseUrl() + MUTE_CHANNEL_UPDATE;
    }


    public String getCreateMultipleChannelUrl() {
        return getBaseUrl() + CREATE_MULTIPLE_CHANNEL_URL;
    }

    public String getAddMemberToGroup() {
        return getBaseUrl() + ADD_MEMBER_TO_CHANNEL_URL;
    }

    public String getRemoveMemberUrl() {
        return getBaseUrl() + REMOVE_MEMBER_FROM_CHANNEL_URL;
    }

    public String getChannelUpdateUrl() {
        return getBaseUrl() + CHANNEL_UPDATE_URL;
    }

    public String getChannelLeftUrl() {
        return getBaseUrl() + CHANNEL_LEFT_URL;
    }

    public String getChannelDeleteUrl() {
        return getBaseUrl() + CHANNEL_DELETE_URL;
    }

    public String getAddMemberToMultipleChannelsUrl() {
        return getBaseUrl() + ADD_MEMBER_TO_MULTIPLE_CHANNELS_URL;
    }

    public String getRemoveMembersFromMultipChannels() {
        return getBaseUrl() + REMOVE_MEMBERS_FROM_MULTIPE_CHANNELS;
    }

    public String addMembersToContactGroupUrl() {
        return getBaseUrl() + ADD_MEMBERS_TO_CONTACT_GROUP_URL;
    }

    public String addMembersToContactGroupOfTypeUrl() {
        return getBaseUrl() + ADD_MEMBERS_TO_CONTACT_GROUP_OF_TYPE_URL;
    }

    public String getMembersFromContactGroupUrl() {
        return getBaseUrl() + GET_MEMBERS_FROM_CONTACT_GROUP_URL;
    }

    public String getMembersFromContactGroupOfTypeUrl() {
        return getBaseUrl() + GET_MEMBERS_TO_CONTACT_GROUP_OF_TYPE_URL;
    }

    private String getMembersFromContactGroupListUrl() {
        return getBaseUrl() + GET_MEMBERS_FROM_CONTACT_GROUP_LIST_URL;
    }

    public String getGroupInfoFromGroupIdsUrl() {
        return getBaseUrl() + GET_GROUP_INFO_FROM_GROUP_IDS_URL;
    }

    public String getRemoveMemberFromGroupTypeUrl() {
        return getBaseUrl() + REMOVE_MEMBERS_FROM_CONTACT_GROUP_OF_TYPE_URL;
    }

    private String getCreateConversationUrl() {
        return getKmBaseUrl() + CREATE_CONVERSATION_URL;
    }

    public ChannelFeed getChannelInfoByParameters(String parameters) {
        String response = "";
        try {
            response = httpRequestUtils.getResponse(getChannelInfoUrl() + "?" + parameters,
                    "application/json", "application/json");
            ChannelFeedApiResponse channelFeedApiResponse = (ChannelFeedApiResponse) GsonUtils
                    .getObjectFromJson(response, ChannelFeedApiResponse.class);
            Utils.printLog(context, TAG, "Channel info response  is :" + response);

            if (channelFeedApiResponse != null && channelFeedApiResponse.isSuccess()) {
                ChannelFeed channelFeed = channelFeedApiResponse.getResponse();
                return channelFeed;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ChannelFeed getChannelInfo(String clientGroupId) {
        return getChannelInfoByParameters(CLIENT_GROUPID + "=" + clientGroupId);
    }

    public ChannelFeed getChannelInfo(Integer channelKey) {
        return getChannelInfoByParameters(GROUP_ID + "=" + channelKey);
    }


    public ApiResponse muteNotification(MuteNotificationRequest muteNotificationRequest) {
        ApiResponse apiResponse = null;

        try {
            if (muteNotificationRequest.isRequestValid()) {
                String requestJson = GsonUtils.getJsonFromObject(muteNotificationRequest,
                        MuteNotificationRequest.class);
                String response = httpRequestUtils.postData(getMuteChannelUrl(),
                        "application/json", "application/json", requestJson);
                apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse
                        .class);

                if (apiResponse != null) {
                    Utils.printLog(context, TAG, "Mute notification response: " + apiResponse
                            .getStatus());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return apiResponse;
    }

    public SyncChannelFeed getChannelFeed(String lastChannelSyncTime) {
        String url = getChannelSyncUrl() + "?" +
                UPDATED_AT
                + "=" + lastChannelSyncTime;
        try {
            String response = httpRequestUtils.getResponse(url, "application/json",
                    "application/json");
            Utils.printLog(context, TAG, "Channel sync call response: " + response);
            return (SyncChannelFeed) GsonUtils.getObjectFromJson(response, SyncChannelFeed.class);
        } catch (Exception e) {
            return null;
        }
    }

    @Deprecated
    public ChannelFeed createChannel(ChannelInfo channelInfo) {
        ChannelFeed channelFeed = null;
        try {
            String jsonFromObject = GsonUtils.getJsonFromObject(channelInfo, channelInfo.getClass
                    ());
            String createChannelResponse = httpRequestUtils.postData(getCreateChannelUrl(),
                    "application/json", "application/json", jsonFromObject);
            Utils.printLog(context, TAG, "Create channel Response :" + createChannelResponse);
            ChannelFeedApiResponse channelFeedApiResponse = (ChannelFeedApiResponse) GsonUtils
                    .getObjectFromJson(createChannelResponse, ChannelFeedApiResponse.class);

            if (channelFeedApiResponse != null && channelFeedApiResponse.isSuccess()) {
                channelFeed = channelFeedApiResponse.getResponse();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return channelFeed;
    }

    public ChannelFeedApiResponse createChannelWithReponse(ChannelInfo channelInfo) throws Exception {
        String jsonFromObject = GsonUtils.getJsonFromObject(channelInfo, channelInfo.getClass());
        String createChannelResponse = httpRequestUtils.postData(getCreateChannelUrl(),
                "application/json", "application/json", jsonFromObject);
        Utils.printLog(context, TAG, "Create channel Response :" + createChannelResponse);
        return (ChannelFeedApiResponse) GsonUtils.getObjectFromJson(createChannelResponse,
                ChannelFeedApiResponse.class);
    }

    public List<ChannelFeed> createMultipleChannels(List<ChannelInfo> channels) {
        List<ChannelFeed> channelFeeds = null;
        try {
            String jsonFromObject = GsonUtils.getJsonFromObject(channels, new
                    TypeToken<List<ChannelInfo>>() {
                    }.getType());
            String createChannelResponse = httpRequestUtils.postData(getCreateMultipleChannelUrl
                    (), "application/json", "application/json", jsonFromObject);
            Utils.printLog(context, TAG, "Create Multiple channel Response :" +
                    createChannelResponse);
            MultipleChannelFeedApiResponse channelFeedApiResponse =
                    (MultipleChannelFeedApiResponse) GsonUtils.getObjectFromJson
                            (createChannelResponse, MultipleChannelFeedApiResponse.class);

            if (channelFeedApiResponse != null && channelFeedApiResponse.isSuccess()) {
                channelFeeds = channelFeedApiResponse.getResponse();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return channelFeeds;
    }

    public ApiResponse removeMembersFromMultipleChannelsByChannelKeys(Set<Integer> channelKeys,
                                                                      Set<String> userIds) {
        return removeMembersFromMultipleChannels(null, channelKeys, userIds);
    }

    public ApiResponse removeMembersFromMultipleChannelsByClientGroupIds(Set<String>
                                                                                 clientGroupIds,
                                                                         Set<String> userIds) {
        return removeMembersFromMultipleChannels(clientGroupIds, null, userIds);
    }

    private ApiResponse removeMembersFromMultipleChannels(Set<String> clientGroupIds,
                                                          Set<Integer> channelKeys, Set<String>
                                                                  userIds) {
        ApiResponse apiResponse = null;
        try {
            if (userIds != null && userIds.size() > 0) {
                String parameters = "";
                if (clientGroupIds != null && clientGroupIds.size() > 0) {
                    for (String clientGroupId : clientGroupIds) {
                        parameters += CLIENT_GROUPIDs + "=" + URLEncoder.encode(clientGroupId,
                                "UTF-8") + "&";
                    }
                } else if (channelKeys != null && channelKeys.size() > 0) {
                    for (Integer channelKey : channelKeys) {
                        parameters += GROUPIDS + "=" + channelKey + "&";
                    }
                }
                for (String userId : userIds) {
                    parameters += USER_ID + "=" + URLEncoder.encode(userId, "UTF-8") + "&";
                }
                String url = getRemoveMembersFromMultipChannels() + "?" + parameters;
                String response = httpRequestUtils.getResponse(url, "application/json",
                        "application/json");
                apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse
                        .class);
                if (apiResponse != null) {
                    Utils.printLog(context, TAG, "Channel remove members from channels response: " +
                            "" + apiResponse.getStatus());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return apiResponse;
    }


    public synchronized ApiResponse addMemberToMultipleChannels(Set<String> clientGroupIds,
                                                                Set<Integer> channelKeys, String
                                                                        userId) {
        ApiResponse apiResponse = null;
        try {
            if (!TextUtils.isEmpty(userId)) {
                String parameters = "";
                if (clientGroupIds != null && clientGroupIds.size() > 0) {
                    for (String clientGroupId : clientGroupIds) {
                        parameters += CLIENT_GROUPIDs + "=" + URLEncoder.encode(clientGroupId,
                                "UTF-8") + "&";
                    }
                } else {
                    for (Integer channelKey : channelKeys) {
                        parameters += GROUPIDS + "=" + channelKey + "&";
                    }
                }
                String url = getAddMemberToMultipleChannelsUrl() + "?" + parameters + USER_ID +
                        "=" + URLEncoder.encode(userId, "UTF-8");
                String response = httpRequestUtils.getResponse(url, "application/json",
                        "application/json");
                apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse
                        .class);
                if (apiResponse != null) {
                    Utils.printLog(context, TAG, "Channel add member call response: " +
                            apiResponse.getStatus());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return apiResponse;
    }

    public ApiResponse addMemberToMultipleChannelsByChannelKey(Set<Integer> channelKeys, String
            userId) {
        return addMemberToMultipleChannels(null, channelKeys, userId);
    }

    public ApiResponse addMemberToMultipleChannelsByClientGroupIds(Set<String> clientGroupIds,
                                                                   String userId) {
        return addMemberToMultipleChannels(clientGroupIds, null, userId);
    }

    public synchronized ApiResponse addMemberToChannel(String clientGroupId, Integer channelKey,
                                                       String userId) {
        try {
            String parameters = "";
            if (!TextUtils.isEmpty(clientGroupId)) {
                parameters = CLIENT_GROUPID + "=" + URLEncoder.encode(clientGroupId, "UTF-8");
            } else {
                parameters = GROUP_ID + "=" + channelKey;
            }
            if (!TextUtils.isEmpty(parameters) && !TextUtils.isEmpty(userId)) {
                String url = getAddMemberToGroup() + "?" +
                        parameters + "&" + USER_ID + "=" + URLEncoder.encode(userId, "UTF-8");
                String response = httpRequestUtils.getResponse(url, "application/json",
                        "application/json");
                ApiResponse apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response,
                        ApiResponse.class);
                if (apiResponse != null) {
                    Utils.printLog(context, TAG, "Channel add member call response: " +
                            apiResponse.getStatus());
                }
                return apiResponse;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized ApiResponse addMemberToChannel(Integer channelKey, String userId) {
        return addMemberToChannel(null, channelKey, userId);
    }

    public synchronized ApiResponse addMemberToChannel(String clientGroupId, String userId) {
        return addMemberToChannel(clientGroupId, null, userId);
    }

    public synchronized ApiResponse removeMemberFromChannel(String clientGroupId, Integer
            channelKey, String userId) {
        ApiResponse apiResponse = null;
        try {
            String parameters = "";
            if (!TextUtils.isEmpty(clientGroupId)) {
                parameters = CLIENT_GROUPID + "=" + URLEncoder.encode(clientGroupId, "UTF-8");
            } else {
                parameters = GROUP_ID + "=" + channelKey;
            }
            if (!TextUtils.isEmpty(parameters) && !TextUtils.isEmpty(userId)) {
                String url = getRemoveMemberUrl() + "?" +
                        parameters + "&" + USER_ID + "=" + URLEncoder.encode(userId, "UTF-8");
                String response = httpRequestUtils.getResponse(url, "application/json",
                        "application/json");
                apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse
                        .class);
                if (apiResponse != null) {
                    Utils.printLog(context, TAG, "Channel remove member response: " + apiResponse
                            .getStatus());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return apiResponse;
    }

    public synchronized ApiResponse removeMemberFromChannel(Integer channelKey, String userId) {
        return removeMemberFromChannel(null, channelKey, userId);
    }

    public synchronized ApiResponse removeMemberFromChannel(String clientGroupId, String userId) {
        return removeMemberFromChannel(clientGroupId, null, userId);
    }

    public synchronized ApiResponse updateChannel(GroupInfoUpdate groupInfoUpdate) {
        ApiResponse apiResponse = null;
        try {
            if (groupInfoUpdate != null) {
                String channelNameUpdateJson = GsonUtils.getJsonFromObject(groupInfoUpdate,
                        GroupInfoUpdate.class);
                String response = httpRequestUtils.postData(getChannelUpdateUrl(),
                        "application/json", "application/json", channelNameUpdateJson);
                apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse
                        .class);
                if (apiResponse != null) {
                    Utils.printLog(context, TAG, "Update Channel response: " + apiResponse
                            .getStatus());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return apiResponse;
    }

    public ApiResponse leaveMemberFromChannel(Integer channelKey) {
        return leaveMemberFromChannel(null, channelKey);
    }

    public ApiResponse leaveMemberFromChannel(String clientGroupId) {
        return leaveMemberFromChannel(clientGroupId, null);
    }

    public synchronized ApiResponse leaveMemberFromChannel(String clientGroupId, Integer
            channelKey) {
        ApiResponse apiResponse = null;
        try {
            String parameters = "";
            if (!TextUtils.isEmpty(clientGroupId)) {
                parameters = CLIENT_GROUPID + "=" + URLEncoder.encode(clientGroupId, "UTF-8");
            } else {
                parameters = GROUP_ID + "=" + channelKey;
            }

            if (!TextUtils.isEmpty(clientGroupId) || (channelKey != null && channelKey != 0)) {
                String url = getChannelLeftUrl() + "?" + parameters;
                String response = httpRequestUtils.getResponse(url, "application/json",
                        "application/json");
                apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse
                        .class);
                if (apiResponse != null) {
                    Utils.printLog(context, TAG, "Channel leave member call response: " +
                            apiResponse.getStatus());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return apiResponse;
    }

    public synchronized ApiResponse deleteChannel(Integer channelKey) {
        return deleteChannel(channelKey, false, false);
    }

    public synchronized ApiResponse deleteChannel(Integer channelKey, boolean updateClientGroupId, boolean resetCount) {
        try {
            if (channelKey != null) {
                StringBuilder urlBuilder = new StringBuilder(getChannelDeleteUrl());
                urlBuilder.append("?").append(GROUP_ID).append("=").append(URLEncoder.encode(String.valueOf(channelKey), "UTF-8"));

                if (updateClientGroupId) {
                    urlBuilder.append("&").append(UPDATE_CLIENT_GROUP_ID).append("=").append("true");
                }

                if (resetCount) {
                    urlBuilder.append("&").append(RESET_UNREAD_COUNT).append("=").append("true");
                }

                String response = httpRequestUtils.getResponse(urlBuilder.toString(), "application/json",
                        "application/json");
                ApiResponse apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response,
                        ApiResponse.class);
                if (apiResponse != null) {
                    Utils.printLog(context, TAG, "Channel delete call response: " + apiResponse
                            .getStatus());
                }
                return apiResponse;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public ChannelFeedApiResponse createChannelWithResponse(ChannelInfo channelInfo) {
        try {
            String jsonFromObject = GsonUtils.getJsonFromObject(channelInfo, channelInfo.getClass
                    ());
            String createChannelResponse = httpRequestUtils.postData(getCreateChannelUrl(),
                    "application/json", "application/json", jsonFromObject);
            Utils.printLog(context, TAG, "Create channel Response :" + createChannelResponse);
            if (TextUtils.isEmpty(createChannelResponse)) {
                return null;
            }
            return (ChannelFeedApiResponse) GsonUtils.getObjectFromJson(createChannelResponse,
                    ChannelFeedApiResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ApiResponse addMemberToContactGroup(String contactGroupId, List<String>
            contactGroupMemberList) {
        String response;
        if (!TextUtils.isEmpty(contactGroupId) && contactGroupMemberList != null) {
            String url = String.format(addMembersToContactGroupUrl(), contactGroupId);
            Utils.printLog(context, TAG, url);
            String jsonFromObject = GsonUtils.getJsonFromObject(contactGroupMemberList, List.class);
            Utils.printLog(context, TAG, "Sending json:" + jsonFromObject);
            try {
                response = httpRequestUtils.postData(url, "application/json", "application/json",
                        jsonFromObject);
                ApiResponse apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response,
                        ApiResponse.class);

                if (apiResponse != null) {
                    Utils.printLog(context, TAG, "Add Member To Contact Group Response: " +
                            apiResponse.getStatus());
                    return apiResponse;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public ApiResponse addMemberToContactGroupOfType(String contactGroupId, String groupType,
                                                     List<String> contactGroupMemberList) {
        String response;
        if (!TextUtils.isEmpty(contactGroupId) && !TextUtils.isEmpty(groupType) &&
                contactGroupMemberList != null) {
            String url = String.format(addMembersToContactGroupOfTypeUrl(), contactGroupId);
            ApplozicAddMemberOfGroupType applozicAddMemberOfGroupType = new
                    ApplozicAddMemberOfGroupType();
            applozicAddMemberOfGroupType.setGroupMemberList(contactGroupMemberList);
            applozicAddMemberOfGroupType.setType(groupType);
            String jsonFromObject = GsonUtils.getJsonFromObject(applozicAddMemberOfGroupType,
                    ApplozicAddMemberOfGroupType.class);
            Utils.printLog(context, TAG, "Sending json:" + jsonFromObject);
            try {
                response = httpRequestUtils.postData(url, "application/json", "application/json",
                        jsonFromObject);
                ApiResponse apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response,
                        ApiResponse.class);

                if (apiResponse != null) {
                    Utils.printLog(context, TAG, "Add Member To Contact Group Response: " +
                            apiResponse.getStatus());
                    return apiResponse;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public ChannelFeed getMembersFromContactGroup(String contactGroupId) {
        String response;
        if (!TextUtils.isEmpty(contactGroupId)) {
            String url = String.format(getMembersFromContactGroupUrl(), contactGroupId);
            response = httpRequestUtils.getResponse(url, "application/json", "application/json");
            ChannelFeedApiResponse channelFeedApiResponse = (ChannelFeedApiResponse) GsonUtils
                    .getObjectFromJson(response, ChannelFeedApiResponse.class);
            if (channelFeedApiResponse != null && channelFeedApiResponse.isSuccess()) {
                ChannelFeed channelFeed = channelFeedApiResponse.getResponse();
                return channelFeed;
            }
        }
        return null;
    }

    public ChannelFeed getMembersFromContactGroupOfType(String contactGroupId, String groupType) {
        String response;
        if (!TextUtils.isEmpty(contactGroupId) && !TextUtils.isEmpty(groupType)) {
            String url = String.format(getMembersFromContactGroupOfTypeUrl() + "?" + GROUPTYPE +
                    "=" + groupType, contactGroupId);
            response = httpRequestUtils.getResponse(url, "application/json", "application/json");
            ChannelFeedApiResponse channelFeedApiResponse = (ChannelFeedApiResponse) GsonUtils
                    .getObjectFromJson(response, ChannelFeedApiResponse.class);
            if (channelFeedApiResponse != null && channelFeedApiResponse.isSuccess()) {
                ChannelFeed channelFeed = channelFeedApiResponse.getResponse();
                return channelFeed;
            }
        }
        return null;
    }

    public ChannelFeedListResponse getGroupInfoFromGroupIds(List<String> groupIds, List<String>
            clientGroupIds) {
        ChannelFeedListResponse apiResponse = null;

        try {
            StringBuilder parameters = new StringBuilder("?");

            if (groupIds != null) {
                for (String groupId : groupIds) {
                    if (!TextUtils.isEmpty(groupId)) {
                        parameters.append(GROUPIDS + "=" + groupId + "&");
                    }
                }
            }

            if (clientGroupIds != null) {
                for (String clientGroupId : clientGroupIds) {
                    if (!TextUtils.isEmpty(clientGroupId)) {
                        if (groupIds != null && groupIds.contains(clientGroupId)) {
                            continue;
                        } else {
                            parameters.append(CLIENT_GROUPIDs + "=" + clientGroupId + "&");
                        }
                    }
                }
            }

            String url = getGroupInfoFromGroupIdsUrl() + parameters;
            String response = httpRequestUtils.getResponse(url, "application/json",
                    "application/json");
            apiResponse = (ChannelFeedListResponse) GsonUtils.getObjectFromJson(response,
                    ChannelFeedListResponse.class);

            if (apiResponse != null) {
                Utils.printLog(context, TAG, "Group Info from groupIds/clientGroupIds response : " +
                        "" + apiResponse.getStatus());
            }
        } catch (Exception e) {
            Utils.printLog(context, TAG, e.getMessage());
        }
        return apiResponse;
    }

    public ChannelFeedListResponse getMemebersFromContactGroupIds(List<String> groupIds,
                                                                  List<String> groupNames, String
                                                                          groupType) {
        ChannelFeedListResponse channelFeedListResponse = null;

        try {
            StringBuilder parameters = new StringBuilder("?");

            if (!TextUtils.isEmpty(groupType)) {
                parameters.append(GROUPTYPE + "=" + groupType + "&");
            }

            if (groupIds != null) {
                for (String groupId : groupIds) {
                    if (!TextUtils.isEmpty(groupId)) {
                        parameters.append(GROUP_ID + "=" + groupId + "&");
                    }
                }
            }

            if (groupNames != null) {
                for (String groupName : groupNames) {
                    if (!TextUtils.isEmpty(groupName)) {
                        parameters.append(GROUP_NAME + "=" + groupName + "&");

                    }
                }
            }

            String url = getMembersFromContactGroupListUrl() + parameters;

            String response = httpRequestUtils.getResponse(url, "application/json",
                    "application/json");
            channelFeedListResponse = (ChannelFeedListResponse) GsonUtils.getObjectFromJson
                    (response, ChannelFeedListResponse.class);

            if (channelFeedListResponse != null) {
                Utils.printLog(context, TAG, "Get Memebers from Contact Group List of Type " +
                        "Response : " + channelFeedListResponse.getStatus());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return channelFeedListResponse;
    }

    public String createConversation(Integer groupId, String userId, String agentId, String
            applicationId) {

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("groupId", groupId);
            jsonObject.put("participentUserId", userId);
            jsonObject.put("createdBy", userId);
            jsonObject.put("defaultAgentId", agentId);
            jsonObject.put("applicationId", applicationId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            String response = httpRequestUtils.postData(getCreateConversationUrl(),
                    "application/json", "application/json", jsonObject.toString());
            Utils.printLog(context, TAG, "Response : " + response);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ApiResponse removeMemberFromContactGroupOfType(String groupName, String groupType,
                                                          String userId) {
        String response;
        String parameters;
        String url;
        if (!TextUtils.isEmpty(groupName) && !TextUtils.isEmpty(userId)) {
            if (!TextUtils.isEmpty(groupType)) {
                parameters = "?" + USER_ID + "=" + userId + "&" + GROUPTYPE + "=" + groupType;
            } else {
                parameters = "?" + USER_ID + "=" + userId;
            }
            url = String.format(getRemoveMemberFromGroupTypeUrl() + parameters, groupName);
            try {
                response = httpRequestUtils.getResponse(url, "application/json",
                        "application/json");
                ApiResponse apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response,
                        ApiResponse.class);
                if (apiResponse != null) {
                    Utils.printLog(context, TAG, "Remove memeber from Group of Type Response: " +
                            apiResponse.getStatus());
                    return apiResponse;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}