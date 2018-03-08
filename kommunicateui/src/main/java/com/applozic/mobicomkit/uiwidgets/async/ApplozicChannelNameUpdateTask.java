package com.applozic.mobicomkit.uiwidgets.async;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.feed.GroupInfoUpdate;
import com.applozic.mobicomkit.uiwidgets.R;

/**
 * Created by sunil on 17/5/16.
 */
public class ApplozicChannelNameUpdateTask extends AsyncTask<Void, Void, Boolean> {

    Context context;
    Integer channelKey;
    String channelNewName;
    ChannelNameUpdateListener channelNameUpdateListener;
    ChannelService channelService;
    Exception exception;
    String updateNameResponse;
    String clientGroupId;
    String imageUrl;
    GroupInfoUpdate groupInfoUpdate;

    public ApplozicChannelNameUpdateTask(Context context, Integer channelKey, String channelName, ChannelNameUpdateListener channelNameUpdateListener) {
        this.channelKey = channelKey;
        this.channelNewName = channelName;
        this.channelNameUpdateListener = channelNameUpdateListener;
        this.context = context;
        this.channelService = ChannelService.getInstance(context);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            if (!TextUtils.isEmpty(channelNewName) && channelNewName.trim().length() != 0) {
                if (channelKey != null && channelKey != 0) {
                    groupInfoUpdate = new GroupInfoUpdate(channelNewName.trim(), channelKey);
                } else if (!TextUtils.isEmpty(clientGroupId)) {
                    groupInfoUpdate = new GroupInfoUpdate(channelNewName.trim(), clientGroupId);
                }
                if (groupInfoUpdate != null && !TextUtils.isEmpty(imageUrl)) {
                    groupInfoUpdate.setImageUrl(imageUrl);
                }
                updateNameResponse = channelService.updateChannel(groupInfoUpdate);
                if (!TextUtils.isEmpty(updateNameResponse)) {
                    return MobiComKitConstants.SUCCESS.equals(updateNameResponse);
                }
            } else {
                throw new Exception(context.getString(R.string.applozic_userId_error_info_in_logs));
            }
        } catch (Exception e) {
            e.printStackTrace();
            exception = e;
            return false;
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean resultBoolean) {
        super.onPostExecute(resultBoolean);

        if (resultBoolean && channelNameUpdateListener != null) {
            channelNameUpdateListener.onSuccess(updateNameResponse, context);
        } else if (!resultBoolean && exception != null && channelNameUpdateListener != null) {
            channelNameUpdateListener.onFailure(updateNameResponse, exception, context);
        }
    }

    public interface ChannelNameUpdateListener {
        void onSuccess(String response, Context context);

        void onFailure(String response, Exception e, Context context);
    }
}
