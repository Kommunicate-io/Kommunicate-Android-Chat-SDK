package io.kommunicate.devkit.channel.service;

import android.content.Context;

import androidx.annotation.VisibleForTesting;

import android.text.TextUtils;

import io.kommunicate.devkit.SettingsSharedPreference;
import io.kommunicate.devkit.api.account.user.MobiComUserPreference;
import io.kommunicate.devkit.api.account.user.UserService;
import io.kommunicate.devkit.api.conversation.MobiComConversationService;
import io.kommunicate.devkit.api.conversation.service.ConversationService;
import io.kommunicate.devkit.api.notification.MuteNotificationRequest;
import io.kommunicate.devkit.api.people.GetPeopleTask;
import io.kommunicate.devkit.api.people.ChannelInfo;
import io.kommunicate.devkit.broadcast.BroadcastService;
import io.kommunicate.devkit.cache.MessageSearchCache;
import io.kommunicate.devkit.channel.database.ChannelDatabaseService;
import io.kommunicate.devkit.contact.AppContactService;
import io.kommunicate.devkit.contact.BaseContactService;
import io.kommunicate.devkit.feed.Response;
import io.kommunicate.devkit.feed.ApiResponse;
import io.kommunicate.devkit.feed.ChannelFeed;
import io.kommunicate.devkit.feed.ChannelFeedApiResponse;
import io.kommunicate.devkit.feed.ChannelFeedListResponse;
import io.kommunicate.devkit.feed.ChannelUsersFeed;
import io.kommunicate.devkit.feed.GroupInfoUpdate;
import io.kommunicate.devkit.listners.ChannelListener;
import io.kommunicate.devkit.sync.SyncChannelFeed;
import io.kommunicate.commons.AppContextService;
import io.kommunicate.commons.people.channel.Channel;
import io.kommunicate.commons.people.channel.ChannelUserMapper;
import io.kommunicate.commons.task.CoreTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by sunil on 1/1/16.
 */
public class ChannelService {

    public static boolean isUpdateTitle = false;
    private static ChannelService channelService;
    public Context context;
    private ChannelDatabaseService channelDatabaseService;
    private ChannelClientService channelClientService;
    private BaseContactService baseContactService;
    private UserService userService;
    private String loggedInUserId;
    private static final String SUCCESS = "success";

    private ChannelService(Context context) {
        this.context = AppContextService.getContext(context);
        channelClientService = ChannelClientService.getInstance(AppContextService.getContext(context));
        channelDatabaseService = ChannelDatabaseService.getInstance(AppContextService.getContext(context));
        userService = UserService.getInstance(AppContextService.getContext(context));
        baseContactService = new AppContactService(AppContextService.getContext(context));
        loggedInUserId = MobiComUserPreference.getInstance(context).getUserId();
    }

    public synchronized static ChannelService getInstance(Context context) {
        if (channelService == null) {
            channelService = new ChannelService(AppContextService.getContext(context));
        }
        return channelService;
    }

    @VisibleForTesting
    public void setChannelClientService(ChannelClientService channelClientService) {
        this.channelClientService = channelClientService;
    }

    @VisibleForTesting
    public void setChannelDatabaseService(ChannelDatabaseService channelDatabaseService) {
        this.channelDatabaseService = channelDatabaseService;
    }

    @VisibleForTesting
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @VisibleForTesting
    public void setContactService(AppContactService appContactService) {
        this.baseContactService = appContactService;
    }

    public Channel getChannelInfoFromLocalDb(Integer key) {
        return channelDatabaseService.getChannelByChannelKey(key);
    }

    public Channel getChannelInfo(Integer key) {
        if (key == null) {
            return null;
        }
        Channel channel = channelDatabaseService.getChannelByChannelKey(key);
        if (channel == null) {
            ChannelFeed channelFeed = channelClientService.getChannelInfo(key);
            if (channelFeed != null) {
                channelFeed.setUnreadCount(0);
                ChannelFeed[] channelFeeds = new ChannelFeed[1];
                channelFeeds[0] = channelFeed;
                processChannelFeedList(channelFeeds, false);
                channel = getChannel(channelFeed);
                return channel;
            }
        }
        return channel;
    }

    public static void clearInstance() {
        channelService = null;
    }

    public String getLoggedInUserId() {
        return loggedInUserId;
    }

    public Channel getChannelInfo(String clientGroupId) {
        if (TextUtils.isEmpty(clientGroupId)) {
            return null;
        }
        Channel channel = channelDatabaseService.getChannelByClientGroupId(clientGroupId);
        if (channel == null) {
            ChannelFeed channelFeed = channelClientService.getChannelInfo(clientGroupId);
            if (channelFeed != null) {
                channelFeed.setUnreadCount(0);
                ChannelFeed[] channelFeeds = new ChannelFeed[1];
                channelFeeds[0] = channelFeed;
                processChannelFeedList(channelFeeds, false);
                channel = getChannel(channelFeed);
                return channel;
            }
        }
        return channel;
    }

    public void createMultipleChannels(List<ChannelInfo> channelInfo) {
        List<ChannelFeed> channelFeeds = channelClientService.createMultipleChannels(channelInfo);
        if (channelFeeds != null) {
            processChannelList(channelFeeds);
        }
    }

    public void processChannelFeedList(ChannelFeed[] channelFeeds, boolean isUserDetails) {
        if (channelFeeds != null && channelFeeds.length > 0) {
            for (ChannelFeed channelFeed : channelFeeds) {
                processChannelFeed(channelFeed, isUserDetails);
            }
        }
    }


    public void processChannelFeed(ChannelFeed channelFeed, boolean isUserDetails) {
        if (channelFeed != null) {
            Set<String> memberUserIds = null;
            if (channelFeed.getMembersName() != null) {
                memberUserIds = channelFeed.getMembersName();
            } else {
                memberUserIds = channelFeed.getContactGroupMembersId();
            }

            Channel channel = getChannel(channelFeed);
            if (channelDatabaseService.isChannelPresent(channel.getKey())) {
                channelDatabaseService.updateChannel(channel);
            } else {
                channelDatabaseService.addChannel(channel);
            }
            if (channelFeed.getConversationPxy() != null) {
                channelFeed.getConversationPxy().setGroupId(channelFeed.getId());
                ConversationService.getInstance(context).addConversation(channelFeed
                        .getConversationPxy());
            }
            if (memberUserIds != null && memberUserIds.size() > 0) {
                for (String userId : memberUserIds) {
                    ChannelUserMapper channelUserMapper = new ChannelUserMapper(channelFeed.getId
                            (), userId);
                    channelUserMapper.setParentKey(channelFeed.getParentKey());
                    if (channelDatabaseService.isChannelUserPresent(channelFeed.getId(), userId)) {
                        channelDatabaseService.updateChannelUserMapper(channelUserMapper);
                    } else {
                        channelDatabaseService.addChannelUserMapper(channelUserMapper);
                    }
                }
            }

            if (isUserDetails) {
                userService.processUserDetail(channelFeed.getUsers());
            }

            if (channelFeed.getGroupUsers() != null && channelFeed.getGroupUsers().size() > 0) {
                for (ChannelUsersFeed channelUsers : channelFeed.getGroupUsers()) {
                    if (channelUsers.getRole() != null) {
                        channelDatabaseService.updateRoleInChannelUserMapper(channelFeed.getId(),
                                channelUsers.getUserId(), channelUsers.getRole());
                    }
                }
            }

            if (channelFeed.getChildKeys() != null && channelFeed.getChildKeys().size() > 0) {
                processChildGroupKeys(channelFeed.getChildKeys());
            }
        }
    }

    public Channel getChannelByChannelKey(Integer channelKey) {
        if (channelKey == null) {
            return null;
        }
        return channelDatabaseService.getChannelByChannelKey(channelKey);
    }


    public List<ChannelUserMapper> getListOfUsersFromChannelUserMapper(Integer channelKey) {
        return channelDatabaseService.getChannelUserList(channelKey);
    }

    public Channel getChannel(Integer channelKey) {
        Channel channel;
        channel = MessageSearchCache.getChannelByKey(channelKey);
        if (channel == null) {
            channel = channelDatabaseService.getChannelByChannelKey(channelKey);
        }
        if (channel == null) {
            channel = new Channel(channelKey);
        }
        return channel;
    }

    public void updateChannel(Channel channel) {
        if (channelDatabaseService.getChannelByChannelKey(channel.getKey()) == null) {
            channelDatabaseService.addChannel(channel);
        } else {
            channelDatabaseService.updateChannel(channel);
        }
    }

    public List<Channel> getChannelList() {
        return channelDatabaseService.getAllChannels();
    }

    public synchronized void syncChannels(boolean isMetadataUpdate) {
        try {
            final MobiComUserPreference userpref = MobiComUserPreference.getInstance(context);
            SyncChannelFeed syncChannelFeed = channelClientService.getChannelFeed(userpref
                    .getChannelSyncTime());
            if (syncChannelFeed == null || !syncChannelFeed.isSuccess()) {
                return;
            }
            if (syncChannelFeed.getResponse() != null) {
                processChannelList(syncChannelFeed.getResponse());

                BroadcastService.sendUpdate(context, isMetadataUpdate, BroadcastService
                        .INTENT_ACTIONS.CHANNEL_SYNC.toString());
            }
            if(!TextUtils.isEmpty(syncChannelFeed.getGeneratedAt())) {
                userpref.setChannelSyncTime(syncChannelFeed.getGeneratedAt());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void syncChannels() {
        syncChannels(false);
    }

    public synchronized Response createChannel(final ChannelInfo channelInfo) {

        if (channelInfo == null) {
            return null;
        }

        Response response = new Response();
        ChannelFeedApiResponse channelFeedResponse = null;

        try {
            channelFeedResponse = channelClientService.createChannelWithResponse(channelInfo);

            if (channelFeedResponse == null) {
                return null;
            }

            if (channelFeedResponse.isSuccess()) {
                response.setStatus(Response.SUCCESS);
                ChannelFeed channelFeed = channelFeedResponse.getResponse();

                if (channelFeed != null) {
                    ChannelFeed[] channelFeeds = new ChannelFeed[1];
                    channelFeeds[0] = channelFeed;
                    processChannelFeedList(channelFeeds, true);
                    response.setResponse(getChannel(channelFeed));
                }
            } else {
                response.setStatus(Response.ERROR);
                response.setResponse(channelFeedResponse.getErrorResponse());
            }
        } catch (Exception e) {
            response.setStatus(Response.ERROR);
            response.setException(e);
        }

        return response;
    }

    public Channel getChannel(ChannelFeed channelFeed) {
        Channel channel = new Channel(channelFeed.getId(), channelFeed.getName(), channelFeed
                .getAdminName(), channelFeed.getType(), channelFeed.getUnreadCount(), channelFeed
                .getImageUrl());
        channel.setClientGroupId(channelFeed.getClientGroupId());
        channel.setNotificationAfterTime(channelFeed.getNotificationAfterTime());
        channel.setDeletedAtTime(channelFeed.getDeletedAtTime());
        channel.setMetadata(channelFeed.getMetadata());
        channel.setParentKey(channelFeed.getParentKey());
        channel.setParentClientGroupId(channelFeed.getParentClientGroupId());
        channel.setKmStatus(channel.generateKmStatus(loggedInUserId));
        return channel;
    }

    public String removeMemberFromChannelProcess(Integer channelKey, String userId) {
        if (channelKey == null && TextUtils.isEmpty(userId)) {
            return "";
        }
        ApiResponse apiResponse = channelClientService.removeMemberFromChannel(channelKey, userId);
        if (apiResponse == null) {
            return null;
        }
        if (apiResponse.isSuccess()) {
            channelDatabaseService.removeMemberFromChannel(channelKey, userId);
        }
        return apiResponse.getStatus();
    }


    public String removeMemberFromChannelProcess(String clientGroupId, String userId) {
        if (clientGroupId == null && TextUtils.isEmpty(userId)) {
            return "";
        }
        ApiResponse apiResponse = channelClientService.removeMemberFromChannel(clientGroupId,
                userId);
        if (apiResponse == null) {
            return null;
        }
        if (apiResponse.isSuccess()) {
            channelDatabaseService.removeMemberFromChannel(clientGroupId, userId);
        }
        return apiResponse.getStatus();

    }

    @Deprecated
    public String addMemberToChannelProcess(Integer channelKey, String userId) {
        if (channelKey == null && TextUtils.isEmpty(userId)) {
            return "";
        }
        ApiResponse apiResponse = channelClientService.addMemberToChannel(channelKey, userId);
        if (apiResponse == null) {
            return null;
        }
        if (apiResponse.isSuccess()) {
            ChannelUserMapper channelUserMapper = new ChannelUserMapper(channelKey, userId);
            channelDatabaseService.addChannelUserMapper(channelUserMapper);
        }
        return apiResponse.getStatus();
    }

    @Deprecated
    public String addMemberToChannelProcess(String clientGroupId, String userId) {
        if (TextUtils.isEmpty(clientGroupId) && TextUtils.isEmpty(userId)) {
            return "";
        }
        ApiResponse apiResponse = channelClientService.addMemberToChannel(clientGroupId, userId);
        if (apiResponse == null) {
            return null;
        }
        return apiResponse.getStatus();
    }

    public ApiResponse addMemberToChannelProcessWithResponse(String clientGroupId, String userId) {
        if (TextUtils.isEmpty(clientGroupId) && TextUtils.isEmpty(userId)) {
            return null;
        }
        ApiResponse apiResponse = channelClientService.addMemberToChannel(clientGroupId, userId);
        if (apiResponse == null) {
            return null;
        }
        return apiResponse;
    }

    public ApiResponse addMemberToChannelProcessWithResponse(Integer channelKey, String userId) {
        if (channelKey == null && TextUtils.isEmpty(userId)) {
            return null;
        }
        ApiResponse apiResponse = channelClientService.addMemberToChannel(channelKey, userId);
        if (apiResponse == null) {
            return null;
        }
        if (apiResponse.isSuccess()) {
            ChannelUserMapper channelUserMapper = new ChannelUserMapper(channelKey, userId);
            channelDatabaseService.addChannelUserMapper(channelUserMapper);
        }
        return apiResponse;
    }

    public String addMemberToMultipleChannelsProcess(Set<String> clientGroupIds, String userId) {
        if (clientGroupIds == null && TextUtils.isEmpty(userId)) {
            return "";
        }
        ApiResponse apiResponse = channelClientService
                .addMemberToMultipleChannelsByClientGroupIds(clientGroupIds, userId);
        if (apiResponse == null) {
            return null;
        }
        return apiResponse.getStatus();
    }

    public String addMemberToMultipleChannelsProcessByChannelKeys(Set<Integer> channelKeys,
                                                                  String userId) {
        if (channelKeys == null && TextUtils.isEmpty(userId)) {
            return "";
        }
        ApiResponse apiResponse = channelClientService.addMemberToMultipleChannelsByChannelKey
                (channelKeys, userId);
        if (apiResponse == null) {
            return null;
        }
        return apiResponse.getStatus();
    }

    public String leaveMemberFromChannelProcess(String clientGroupId, String userId) {
        if (TextUtils.isEmpty(clientGroupId)) {
            return "";
        }
        ApiResponse apiResponse = channelClientService.leaveMemberFromChannel(clientGroupId);
        if (apiResponse == null) {
            return null;
        }
        if (apiResponse.isSuccess()) {
            channelDatabaseService.leaveMemberFromChannel(clientGroupId, userId);
        }
        return apiResponse.getStatus();
    }

    public String leaveMemberFromChannelProcess(Integer channelKey, String userId) {
        if (channelKey == null) {
            return "";
        }
        ApiResponse apiResponse = channelClientService.leaveMemberFromChannel(channelKey);
        if (apiResponse == null) {
            return null;
        }
        if (apiResponse.isSuccess()) {
            channelDatabaseService.leaveMemberFromChannel(channelKey, userId);
        }
        return apiResponse.getStatus();
    }

    public String updateChannel(GroupInfoUpdate groupInfoUpdate) {
        if (groupInfoUpdate == null) {
            return null;
        }
        ApiResponse apiResponse = channelClientService.updateChannel(groupInfoUpdate);
        if (apiResponse == null) {
            return null;
        }
        if (apiResponse.isSuccess()) {
            channelDatabaseService.updateChannel(groupInfoUpdate);
        }
        return apiResponse.getStatus();
    }

    public String createConversation(Integer groupId, String userId, String agentId,
                                                  String applicationId) {
        return channelClientService.createConversation(groupId, userId, agentId, applicationId);
    }

    public void processChannelList(List<ChannelFeed> channelFeedList) {
        if (channelFeedList != null && channelFeedList.size() > 0) {
            for (ChannelFeed channelFeed : channelFeedList) {
                processChannelFeedForSync(channelFeed);
            }
        }
    }

    public ChannelUserMapper getChannelUserMapper(Integer channelKey) {
        return channelDatabaseService.getChannelUserByChannelKey(channelKey);
    }


    public void updateRoleInChannelUserMapper(Integer channelKey, String userId, Integer role) {
        channelDatabaseService.updateRoleInChannelUserMapper(channelKey, userId, role);
    }

    public ChannelUserMapper getChannelUserMapperByUserId(Integer channelKey, String userId) {
        return channelDatabaseService.getChannelUserByChannelKeyAndUserId(channelKey, userId);
    }

    public boolean processIsUserPresentInChannel(Integer channelKey) {
        return channelDatabaseService.isChannelUserPresent(channelKey, MobiComUserPreference
                .getInstance(context).getUserId());
    }

    public boolean isUserAlreadyPresentInChannel(Integer channelKey, String userId) {
        return channelDatabaseService.isChannelUserPresent(channelKey, userId);
    }

    public boolean processIsUserPresentInChannel(String clientGroupId) {
        Channel channel = channelDatabaseService.getChannelByClientGroupId(clientGroupId);
        return channelDatabaseService.isChannelUserPresent(channel.getKey(),
                MobiComUserPreference.getInstance(context).getUserId());
    }

    public boolean isUserAlreadyPresentInChannel(String clientGroupId, String userId) {
        Channel channel = channelDatabaseService.getChannelByClientGroupId(clientGroupId);
        return channelDatabaseService.isChannelUserPresent(channel.getKey(), userId);
    }

    public String processChannelDeleteConversation(Channel channel, Context context) {
        String response = new MobiComConversationService(context).deleteSync(null, channel, null);
        if (!TextUtils.isEmpty(response) && SUCCESS.equals(response)) {
            channelDatabaseService.deleteChannelUserMappers(channel.getKey());
            channelDatabaseService.deleteChannel(channel.getKey());
        }
        return response;

    }

    public void updateChannelLocalImageURI(Integer channelKey, String localImageURI) {
        channelDatabaseService.updateChannelLocalImageURI(channelKey, localImageURI);
    }

    public ApiResponse muteNotifications(MuteNotificationRequest muteNotificationRequest) {

        ApiResponse apiResponse = channelClientService.muteNotification(muteNotificationRequest);

        if (apiResponse == null) {
            return null;
        }
        if (apiResponse.isSuccess()) {
            channelDatabaseService.updateNotificationAfterTime(muteNotificationRequest.getId(),
                    muteNotificationRequest.getNotificationAfterTime());
        }
        return apiResponse;
    }

    public void updateNotificationAfterTime(Integer groupId, Long notificationAfterTime) {
        if (notificationAfterTime != null && groupId != null) {
            channelDatabaseService.updateNotificationAfterTime(groupId,
                    notificationAfterTime);
        }
    }

    public Channel getChannelByClientGroupId(String clientGroupId) {
        if (TextUtils.isEmpty(clientGroupId)) {
            return null;
        }
        return channelDatabaseService.getChannelByClientGroupId(clientGroupId);
    }

    public ChannelFeedApiResponse createChannelWithResponse(ChannelInfo channelInfo) {
        ChannelFeedApiResponse channelFeedApiResponse = channelClientService
                .createChannelWithResponse(channelInfo);
        if (channelFeedApiResponse == null) {
            return null;
        }
        if (channelFeedApiResponse.isSuccess()) {
            ChannelFeed channelFeed = channelFeedApiResponse.getResponse();
            if (channelFeed != null) {
                ChannelFeed[] channelFeeds = new ChannelFeed[1];
                channelFeeds[0] = channelFeed;
                processChannelFeedList(channelFeeds, true);
            }
        }
        return channelFeedApiResponse;
    }

    public ApiResponse addMemberToChannelWithResponseProcess(Integer channelKey, String userId) {
        if (channelKey == null && TextUtils.isEmpty(userId)) {
            return null;
        }
        ApiResponse apiResponse = channelClientService.addMemberToChannel(channelKey, userId);
        if (apiResponse == null) {
            return null;
        }
        if (apiResponse.isSuccess()) {
            ChannelUserMapper channelUserMapper = new ChannelUserMapper(channelKey, userId);
            channelDatabaseService.addChannelUserMapper(channelUserMapper);
        }
        return apiResponse;
    }

    public String getGroupOfTwoReceiverUserId(Integer channelKey) {
        return channelDatabaseService.getGroupOfTwoReceiverId(channelKey);
    }

    @Deprecated
    public Channel createGroupOfTwo(ChannelInfo channelInfo) {
        if (channelInfo == null) {
            return null;
        }
        if (!TextUtils.isEmpty(channelInfo.getClientGroupId())) {
            Channel channel = channelDatabaseService.getChannelByClientGroupId(channelInfo
                    .getClientGroupId());
            if (channel != null) {
                return channel;
            } else {
                ChannelFeedApiResponse channelFeedApiResponse = channelClientService
                        .createChannelWithResponse(channelInfo);
                if (channelFeedApiResponse == null) {
                    return null;
                }
                if (channelFeedApiResponse.isSuccess()) {
                    ChannelFeed channelFeed = channelFeedApiResponse.getResponse();
                    if (channelFeed != null) {
                        ChannelFeed[] channelFeeds = new ChannelFeed[1];
                        channelFeeds[0] = channelFeed;
                        processChannelFeedList(channelFeeds, true);
                        return getChannel(channelFeed);
                    }
                } else {
                    ChannelFeed channelFeed = channelClientService.getChannelInfo(channelInfo
                            .getClientGroupId());
                    if (channelFeed != null) {
                        ChannelFeed[] channelFeeds = new ChannelFeed[1];
                        channelFeeds[0] = channelFeed;
                        processChannelFeedList(channelFeeds, false);
                        return getChannel(channelFeed);
                    }
                }
            }
        }
        return null;
    }

    public Response createGroupOfTwoWithResponse(ChannelInfo channelInfo) {
        if (channelInfo == null) {
            return null;
        }

        Response response = new Response();

        if (!TextUtils.isEmpty(channelInfo.getClientGroupId())) {
            Channel channel = channelDatabaseService.getChannelByClientGroupId(channelInfo
                    .getClientGroupId());
            if (channel != null) {
                response.setStatus(Response.SUCCESS);
                response.setResponse(channel);
            } else {
                ChannelFeedApiResponse channelFeedApiResponse = channelClientService
                        .createChannelWithResponse(channelInfo);
                if (channelFeedApiResponse == null) {
                    response.setStatus(Response.ERROR);
                } else {
                    if (channelFeedApiResponse.isSuccess()) {
                        ChannelFeed channelFeed = channelFeedApiResponse.getResponse();
                        if (channelFeed != null) {
                            ChannelFeed[] channelFeeds = new ChannelFeed[1];
                            channelFeeds[0] = channelFeed;
                            processChannelFeedList(channelFeeds, true);
                            response.setStatus(Response.SUCCESS);
                            response.setResponse(getChannel(channelFeed));
                        }
                    } else {
                        ChannelFeed channelFeed = channelClientService.getChannelInfo(channelInfo
                                .getClientGroupId());
                        if (channelFeed != null) {
                            ChannelFeed[] channelFeeds = new ChannelFeed[1];
                            channelFeeds[0] = channelFeed;
                            processChannelFeedList(channelFeeds, false);
                            response.setStatus(Response.SUCCESS);
                            response.setResponse(getChannel(channelFeed));
                        }
                    }
                }
            }
        }
        return response;
    }

    public List<ChannelFeed> getGroupInfoFromGroupIds(List<String> groupIds) {
        return getGroupInfoFromGroupIds(groupIds, null);
    }

    public List<ChannelFeed> getGroupInfoFromClientGroupIds(List<String> clientGroupIds) {
        return getGroupInfoFromGroupIds(null, clientGroupIds);
    }

    public List<String> getChildGroupKeys(Integer parentGroupKey) {
        return channelDatabaseService.getChildGroupIds(parentGroupKey);
    }

    public List<ChannelFeed> getGroupInfoFromGroupIds(List<String> groupIds, List<String>
            clientGroupIds) {

        ChannelFeedListResponse channelFeedList = channelClientService.getGroupInfoFromGroupIds
                (groupIds, clientGroupIds);

        if (channelFeedList == null) {
            return null;
        }

        if (channelFeedList != null && ChannelFeedListResponse.SUCCESS.equals(channelFeedList
                .getStatus())) {
            processChannelFeedList(channelFeedList.getResponse().toArray(new
                    ChannelFeed[channelFeedList.getResponse().size()]), false);
        }

        return channelFeedList.getResponse();
    }

    public boolean addMemberToContactGroup(String contactGroupId, String groupType, List<String>
            contactGroupMemberList) {

        ApiResponse apiResponse = null;
        if (!TextUtils.isEmpty(contactGroupId) && contactGroupMemberList != null) {
            if (!TextUtils.isEmpty(groupType)) {
                apiResponse = channelClientService.addMemberToContactGroupOfType(contactGroupId,
                        groupType, contactGroupMemberList);

            } else {
                apiResponse = channelClientService.addMemberToContactGroup(contactGroupId,
                        contactGroupMemberList);
            }
        }

        if (apiResponse == null) {
            return false;
        }
        return apiResponse.isSuccess();
    }

    public ChannelFeed getMembersFromContactGroup(String contactGroupId, String groupType) {
        ChannelFeed channelFeed = null;
        if (!TextUtils.isEmpty(contactGroupId)) {
            if (!TextUtils.isEmpty(groupType)) {
                channelFeed = channelClientService.getMembersFromContactGroupOfType
                        (contactGroupId, groupType);
            } else {
                channelFeed = channelClientService.getMembersFromContactGroup(contactGroupId);
            }
        }
        if (channelFeed != null) {
            ChannelFeed[] channelFeeds = new ChannelFeed[1];
            channelFeeds[0] = channelFeed;
            processChannelFeedList(channelFeeds, false);
            UserService.getInstance(context).processUserDetails(channelFeed
                    .getContactGroupMembersId());
            return channelFeed;
        }
        return null;
    }

    public ChannelFeed[] getMembersFromContactGroupList(List<String> groupIdList, List<String>
            groupNames, String groupType) {
        List<ChannelFeed> channelFeedList;

        ChannelFeedListResponse channelFeedListResponse = channelClientService
                .getMemebersFromContactGroupIds(groupIdList, groupNames, groupType);
        if (channelFeedListResponse != null && channelFeedListResponse.getStatus().equals
                (ChannelFeedListResponse.SUCCESS)) {
            channelFeedList = channelFeedListResponse.getResponse();
            processChannelFeedList(channelFeedList.toArray(new ChannelFeed[channelFeedList.size()
                    ]), false);
            return channelFeedList.toArray(new ChannelFeed[channelFeedList.size()]);
        }

        return null;
    }

    public ApiResponse removeMemberFromContactGroup(String contactGroupId, String groupType,
                                                    String userId) {
        ApiResponse apiResponse;
        if (!TextUtils.isEmpty(contactGroupId) && !TextUtils.isEmpty(userId)) {
            apiResponse = channelClientService.removeMemberFromContactGroupOfType(contactGroupId,
                    groupType, userId);
            return apiResponse;
        }
        return null;
    }


    public void processChannelFeedForSync(ChannelFeed channelFeed) {
        if (channelFeed != null) {
            Set<String> memberUserIds = channelFeed.getMembersName();
            Set<String> userIds = new HashSet<>();
            Channel channel = getChannel(channelFeed);
            if (channelDatabaseService.isChannelPresent(channel.getKey())) {
                channelDatabaseService.updateChannel(channel);
                channelDatabaseService.deleteChannelUserMappers(channel.getKey());
            } else {
                channelDatabaseService.addChannel(channel);
            }
            if (memberUserIds != null && memberUserIds.size() > 0) {
                for (String userId : memberUserIds) {
                    ChannelUserMapper channelUserMapper = new ChannelUserMapper(channelFeed.getId
                            (), userId);
                    channelUserMapper.setParentKey(channelFeed.getParentKey());
                    channelDatabaseService.addChannelUserMapper(channelUserMapper);
                    if (!baseContactService.isContactExists(userId)) {
                        userIds.add(userId);
                    }
                }
                if (userIds != null && userIds.size() > 0) {
                    userService.processUserDetailsByUserIds(userIds);
                }
            }

            if (channelFeed.getGroupUsers() != null && channelFeed.getGroupUsers().size() > 0) {
                for (ChannelUsersFeed channelUsers : channelFeed.getGroupUsers()) {
                    if (channelUsers.getRole() != null) {
                        channelDatabaseService.updateRoleInChannelUserMapper(channelFeed.getId(),
                                channelUsers.getUserId(), channelUsers.getRole());
                    }
                }
            }

            if (channelFeed.getChildKeys() != null && channelFeed.getChildKeys().size() > 0) {
                processChildGroupKeysForChannelSync(channelFeed.getChildKeys());
            }
            if (channel.isDeleted() && SettingsSharedPreference.getInstance(context).isSkipDeletedGroups()) {
                BroadcastService.sendConversationDeleteBroadcast(context, BroadcastService.INTENT_ACTIONS.DELETE_CONVERSATION.toString(), null, channel.getKey(), SUCCESS);
            }
        }
    }

    public String deleteChannel(Integer channelKey) {
        return deleteChannel(channelKey, false, false);
    }

    public String deleteChannel(Integer channelKey, boolean updateClientGroupId, boolean resetCount) {
        ApiResponse apiResponse = channelClientService.deleteChannel(channelKey, updateClientGroupId, resetCount);
        if (apiResponse != null && apiResponse.isSuccess()) {
            channelDatabaseService.deleteChannel(channelKey);
            channelDatabaseService.deleteChannelUserMappers(channelKey);
            BroadcastService.sendConversationDeleteBroadcast(context, BroadcastService.INTENT_ACTIONS.DELETE_CONVERSATION.toString(), null, channelKey, apiResponse.getStatus());
            return apiResponse.getStatus();
        } else {
            return null;
        }
    }

    public void deleteChannelFromDb(Integer channelKey) {
        if(channelKey != null && channelKey != 0) {
            channelDatabaseService.deleteChannel(channelKey);
            channelDatabaseService.deleteChannelUserMappers(channelKey);
            BroadcastService.sendConversationDeleteBroadcast(context, BroadcastService.INTENT_ACTIONS.DELETE_CONVERSATION.toString(), null, channelKey, SUCCESS);

        }
    }

    private void processChildGroupKeys(Set<Integer> childGroupKeys) {
        for (Integer channelKey : childGroupKeys) {
            Channel channel = channelDatabaseService.getChannelByChannelKey(channelKey);
            if (channel == null) {
                ChannelFeed channelFeed = channelClientService.getChannelInfo(channelKey);
                if (channelFeed != null) {
                    processChannelFeed(channelFeed, false);
                }
            }
        }
    }

    private void processChildGroupKeysForChannelSync(Set<Integer> childGroupKeys) {
        for (Integer channelKey : childGroupKeys) {
            Channel channel = channelDatabaseService.getChannelByChannelKey(channelKey);
            if (channel == null) {
                ChannelFeed channelFeed = channelClientService.getChannelInfo(channelKey);
                if (channelFeed != null) {
                    processChannelFeedForSync(channelFeed);
                }
            }
        }
    }

    public Integer getParentGroupKeyByClientGroupKey(String parentClientGroupKey) {
        return channelDatabaseService.getParentGroupKey(parentClientGroupKey);
    }

    public void getChannelByChannelKeyAsync(Integer groupId, ChannelListener channelListener) {
        CoreTask.execute(new GetPeopleTask(context, null, null, groupId, channelListener, null, null, this));
    }

    public void getChannelByClientKeyAsync(String clientChannelKey, ChannelListener channelListener) {
        CoreTask.execute(new GetPeopleTask(context, null, clientChannelKey, null, channelListener, null, null, this));
    }

    /**
     * The getAllChannelList method will return list of groups.
     *
     * @return List of Channel objects or empty list in case of error or no groups.
     */
    public List<Channel> getAllChannelList() {
        List<Channel> channelList = null;
        SyncChannelFeed syncChannelFeed = channelClientService.getChannelFeed(MobiComUserPreference.getInstance(context).getChannelListLastGeneratedAtTime());
        if (syncChannelFeed == null || !syncChannelFeed.isSuccess()) {
            return null;
        }
        List<ChannelFeed> channelFeeds = syncChannelFeed.getResponse();
        if (channelFeeds != null && !channelFeeds.isEmpty()) {
            processChannelFeedList(channelFeeds.toArray(new ChannelFeed[channelFeeds.size()
                    ]), false);
        }
        MobiComUserPreference.getInstance(context).setChannelListLastGeneratedAtTime(syncChannelFeed.getGeneratedAt());

        channelList = ChannelDatabaseService.getInstance(context).getAllChannels();
        if (channelList == null) {
            return new ArrayList<>();
        }
        return channelList;
    }
}