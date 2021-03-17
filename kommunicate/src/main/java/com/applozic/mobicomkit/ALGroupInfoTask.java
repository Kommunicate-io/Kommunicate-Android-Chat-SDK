package com.applozic.mobicomkit;

/**
 * Created by ashish on 24/04/18.
 */

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.HttpRequestUtils;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.channel.database.ChannelDatabaseService;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.feed.ChannelFeed;
import com.applozic.mobicomkit.feed.ChannelFeedApiResponse;
import com.applozic.mobicomkit.feed.ErrorResponseFeed;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.channel.ChannelUserMapper;
import com.applozic.mobicommons.task.AlAsyncTask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by reytum on 20/10/17.
 */

public class ALGroupInfoTask extends AlAsyncTask<Void, ALGroupInfoTask.ChannelModel> {

    private Context context;
    private Integer groupId;
    private String clientGroupId;
    private ChannelInfoListener listener;
    private ChannelDatabaseService channelDatabaseService;
    private ChannelService channelService;
    private boolean isUserListRequest;
    private static final String BASE_URL_METADATA = "com.applozic.server.url";
    private String DEFAULT_URL = "https://apps.applozic.com";
    private static final String CHANNEL_INFO_URL = "/rest/ws/group/info";
    private static final String GROUP_ID = "groupId";
    private static final String CLIENT_GROUPID = "clientGroupId";


    public ALGroupInfoTask(Context context, Integer groupId, String clientGroupId, boolean isUserListRequest, ChannelInfoListener listener) {
        this.context = new WeakReference<Context>(context).get();
        this.groupId = groupId;
        this.clientGroupId = clientGroupId;
        this.listener = listener;
        this.isUserListRequest = isUserListRequest;
        channelDatabaseService = ChannelDatabaseService.getInstance(this.context);
    }

    @Override
    protected ChannelModel doInBackground() {
        ChannelModel model = new ChannelModel();
        Channel channel = null;
        Exception exception = null;

        try {
            if (clientGroupId != null) {
                channel = channelDatabaseService.getChannelByClientGroupId(clientGroupId);
            } else if (groupId != null) {
                channel = channelDatabaseService.getChannelByChannelKey(groupId);
            }
        } catch (Exception e) {
            exception = e;
            e.printStackTrace();
        }

        if (channel != null) {
            model.setChannel(channel);
        } else {
            try {
                if (clientGroupId != null) {
                    model = getChannelInfoByParameters(CLIENT_GROUPID + "=" + clientGroupId);
                } else if (groupId != null) {
                    model = getChannelInfoByParameters(GROUP_ID + "=" + groupId);
                }
            } catch (Exception e) {
                exception = e;
                e.printStackTrace();
            }
        }

        if (model != null) {
            model.setException(exception);
        }
        return model;
    }

    @Override
    protected void onPostExecute(ChannelModel model) {
        super.onPostExecute(model);
        if (listener != null) {
            if (model.getChannel() != null) {
                ChannelInfoModel infoModel = new ChannelInfoModel();

                if (isUserListRequest) {
                    List<ChannelUserMapper> mapperList = ChannelService.getInstance(context).getListOfUsersFromChannelUserMapper(model.getChannel().getKey());
                    ArrayList<String> users = new ArrayList<String>();
                    for (ChannelUserMapper channelUserMapper : mapperList) {
                        users.add(channelUserMapper.getUserKey());
                    }
                    infoModel.setUserList(users);
                }
                infoModel.setChannel(model.getChannel());
                listener.onSuccess(infoModel, "Success, found in local DB", context);
            } else {
                if (model.getChannelFeedApiResponse() != null) {
                    if (model.getChannelFeedApiResponse().isSuccess()) {
                        ChannelFeed channelFeed = model.getChannelFeedApiResponse().getResponse();
                        if (channelFeed != null) {
                            channelService = ChannelService.getInstance(context);
                            channelFeed.setUnreadCount(0);
                            ChannelFeed[] channelFeeds = new ChannelFeed[1];
                            channelFeeds[0] = channelFeed;
                            channelService.processChannelFeedList(channelFeeds, false);
                            Channel channel = channelService.getChannel(channelFeed);
                            if (channel != null) {
                                ChannelInfoModel infoModel = new ChannelInfoModel();

                                if (isUserListRequest) {
                                    ArrayList<String> users = new ArrayList<String>();
                                    users.addAll(channelFeed.getMembersName());
                                    infoModel.setUserList(users);
                                }
                                infoModel.setChannel(channel);

                                listener.onSuccess(infoModel, "Success, fetched from server", context);
                            }
                        }
                    } else {
                        if (model.getChannelFeedApiResponse().getErrorResponse() != null) {
                            listener.onFailure(GsonUtils.getJsonFromObject(model.getChannelFeedApiResponse().getErrorResponse().toArray(new ErrorResponseFeed[model.getChannelFeedApiResponse().getErrorResponse().size()]), ErrorResponseFeed[].class), model.getException(), context);
                        } else {
                            listener.onFailure(null, model.getException(), context);
                        }
                    }
                } else {
                    listener.onFailure(null, model.getException(), context);
                }
            }
        }
    }

    public interface ChannelInfoListener {
        void onSuccess(ChannelInfoModel channelInfoModel, String response, Context context);

        void onFailure(String response, Exception e, Context context);
    }

    public ChannelModel getChannelInfoByParameters(String parameters) {
        String response = "";
        HttpRequestUtils httpRequestUtils = new HttpRequestUtils(context);
        ChannelModel model = new ChannelModel();
        try {
            response = httpRequestUtils.getResponse(getChannelInfoUrl() + "?" + parameters, "application/json", "application/json");
            ChannelFeedApiResponse channelFeedApiResponse = (ChannelFeedApiResponse) GsonUtils.getObjectFromJson(response, ChannelFeedApiResponse.class);
            Utils.printLog(context, "ChannelInfoTask", "Channel info response  is :" + response);
            if (channelFeedApiResponse != null) {
                model.setChannelFeedApiResponse(channelFeedApiResponse);
            }
        } catch (Exception e) {
            model.setException(e);
            e.printStackTrace();
        }
        return model;
    }

    protected String getBaseUrl() {
        String SELECTED_BASE_URL = MobiComUserPreference.getInstance(context).getUrl();

        if (!TextUtils.isEmpty(SELECTED_BASE_URL)) {
            return SELECTED_BASE_URL;
        }
        String BASE_URL = Utils.getMetaDataValue(context.getApplicationContext(), BASE_URL_METADATA);
        if (!TextUtils.isEmpty(BASE_URL)) {
            return BASE_URL;
        }
        return DEFAULT_URL;
    }

    private String getChannelInfoUrl() {
        return getBaseUrl() + CHANNEL_INFO_URL;
    }

    public class ChannelInfoModel {
        Channel channel;
        ArrayList<String> groupMemberList;

        public Channel getChannel() {
            return channel;
        }

        public void setChannel(Channel channel) {
            this.channel = channel;
        }

        public ArrayList<String> getUserList() {
            return groupMemberList;
        }

        public void setUserList(ArrayList<String> userList) {
            this.groupMemberList = userList;
        }

        @Override
        public String toString() {
            return "ChannelInfoModel{" +
                    "channel=" + channel +
                    ", groupMemberList=" + groupMemberList +
                    '}';
        }
    }

    class ChannelModel {
        private ChannelFeedApiResponse channelFeedApiResponse;
        private Exception exception;
        private Channel channel;

        public ChannelFeedApiResponse getChannelFeedApiResponse() {
            return channelFeedApiResponse;
        }

        public void setChannelFeedApiResponse(ChannelFeedApiResponse channelFeedApiResponse) {
            this.channelFeedApiResponse = channelFeedApiResponse;
        }

        public Exception getException() {
            return exception;
        }

        public void setException(Exception exception) {
            this.exception = exception;
        }

        public Channel getChannel() {
            return channel;
        }

        public void setChannel(Channel channel) {
            this.channel = channel;
        }
    }
}
