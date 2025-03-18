package com.applozic.mobicomkit.uiwidgets.async;

import android.content.Context;
import android.os.AsyncTask;

import io.kommunicate.devkit.api.account.user.UserService;
import io.kommunicate.devkit.channel.service.ChannelClientService;
import io.kommunicate.devkit.channel.service.ChannelService;
import io.kommunicate.devkit.feed.ChannelFeed;
import io.kommunicate.devkit.feed.ChannelFeedListResponse;
import io.kommunicate.devkit.feed.ErrorResponseFeed;
import io.kommunicate.commons.json.GsonUtils;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by reytum on 27/10/17.
 */

public class KmGetMembersFromContactGroupListTask extends AsyncTask<Void, Void, KmGetMembersFromContactGroupListTask.AlGetMembersModel> {

    WeakReference<Context> context;
    private String groupType;
    private List<String> groupIds;
    private List<String> groupNames;
    private GetMembersFromGroupIdListListener listener;
    private static final String ERR_OCCURRED = "Some Error occurred";
    private static final String FETCH_SUCCESS = "Successfully fetched";

    public KmGetMembersFromContactGroupListTask(Context context, GetMembersFromGroupIdListListener listener, List<String> groupIds, List<String> groupNames, String groupType) {
        this.context = new WeakReference<Context>(context);
        this.listener = listener;
        this.groupIds = groupIds;
        this.groupNames = groupNames;
        this.groupType = groupType;
    }

    @Override
    protected AlGetMembersModel doInBackground(Void... voids) {

        AlGetMembersModel model = new AlGetMembersModel();

        try {
            ChannelFeedListResponse response = ChannelClientService.getInstance(context.get()).getMemebersFromContactGroupIds(groupIds, groupNames, groupType);
            if (response != null) {
                if (ChannelFeedListResponse.SUCCESS.equals(response.getStatus())) {
                    Set<String> contactIds = new HashSet<String>();
                    if (!response.getResponse().isEmpty()) {
                        ChannelService.getInstance(context.get()).processChannelFeedList(response.getResponse().toArray(new ChannelFeed[response.getResponse().size()]), false);
                        for (ChannelFeed feed : response.getResponse()) {
                            contactIds.addAll(feed.getContactGroupMembersId());
                        }
                        model.setMembers(contactIds.toArray(new String[contactIds.size()]));
                        UserService.getInstance(context.get()).processUserDetailsByUserIds(contactIds);
                        model.setResponse(FETCH_SUCCESS);
                    }
                } else if (response.getErrorResponse() != null) {
                    model.setResponse(GsonUtils.getJsonFromObject(response.getErrorResponse(), ErrorResponseFeed[].class));
                }
            } else {
                model.setResponse(ERR_OCCURRED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.setException(e);
        }
        return model;
    }

    @Override
    protected void onPostExecute(AlGetMembersModel model) {
        if (model != null) {
            if (model.getMembers() != null && model.getMembers().length != 0) {
                listener.onSuccess(context.get(), model.getResponse(), model.getMembers());
            } else {
                listener.onFailure(context.get(), model.getResponse(), model.getException());
            }
        }
        super.onPostExecute(model);
    }

    public interface GetMembersFromGroupIdListListener {
        void onSuccess(Context context, String response, String[] contactList);

        void onFailure(Context context, String response, Exception e);
    }

    public class AlGetMembersModel {
        String[] members;
        String response;
        Exception exception;

        public String[] getMembers() {
            return members;
        }

        public void setMembers(String[] members) {
            this.members = members;
        }

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }

        public Exception getException() {
            return exception;
        }

        public void setException(Exception exception) {
            this.exception = exception;
        }
    }
}
