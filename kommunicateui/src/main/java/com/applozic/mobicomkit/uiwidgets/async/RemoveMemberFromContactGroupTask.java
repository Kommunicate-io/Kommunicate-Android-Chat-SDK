package com.applozic.mobicomkit.uiwidgets.async;

import android.content.Context;
import android.os.AsyncTask;

import io.kommunicate.devkit.channel.service.ChannelService;
import io.kommunicate.devkit.feed.ApiResponse;

import java.lang.ref.WeakReference;

/**
 * Created by reytum on 13/7/17.
 */

public class RemoveMemberFromContactGroupTask extends AsyncTask<Void, Void, ApiResponse> {

    Context context;
    String groupName;
    ApiResponse apiResponse;
    String groupType;
    String userId;
    Exception e;
    RemoveGroupMemberListener listener;
    String failureResponse;
    ChannelService channelService;
    private static final String ERR_OCCURRED = "Some Error occurred";

    public RemoveMemberFromContactGroupTask(Context context, String groupName, String groupType, String userId, RemoveGroupMemberListener listener) {
        this.context = new WeakReference<>(context).get();
        this.groupName = groupName;
        this.groupType = groupType;
        this.userId = userId;
        this.listener = listener;
        this.channelService = ChannelService.getInstance(context);
    }

    @Override
    protected ApiResponse doInBackground(Void... params) {
        try {
            if (groupName != null && userId != null) {
                apiResponse = channelService.removeMemberFromContactGroup(groupName, groupType, userId);
                if (apiResponse != null && !apiResponse.isSuccess()) {
                    e = new Exception(ERR_OCCURRED);
                    failureResponse = apiResponse.toString();
                }
            }
        } catch (Exception e) {
            this.e = e;
            failureResponse = e.getMessage();
            return null;
        }
        return apiResponse;
    }

    @Override
    protected void onPostExecute(ApiResponse apiResponse) {
        super.onPostExecute(apiResponse);
        if (apiResponse != null && apiResponse.isSuccess()) {
            listener.onSuccess(apiResponse.getStatus(), context);
        } else if (e != null) {
            listener.onFailure(failureResponse, e, context);
        }
    }

    public interface RemoveGroupMemberListener {
        void onSuccess(String response, Context context);

        void onFailure(String response, Exception e, Context context);
    }
}
