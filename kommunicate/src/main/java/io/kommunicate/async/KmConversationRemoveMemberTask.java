package io.kommunicate.async;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicommons.ApplozicService;

import java.lang.ref.WeakReference;

import io.kommunicate.R;
import io.kommunicate.callbacks.KmRemoveMemberCallback;

public class KmConversationRemoveMemberTask extends AsyncTask<Void, Void, Boolean> {

    private WeakReference<Context> context;
    private Integer channelKey;
    private String userId;
    private KmRemoveMemberCallback removeMemberCallback;
    private ChannelService channelService;
    private Exception exception;
    private String removeResponse;
    private int index;

    public KmConversationRemoveMemberTask(Context context, Integer channelKey, String userId, int index, KmRemoveMemberCallback removeMemberCallback) {
        this.channelKey = channelKey;
        this.userId = userId;
        this.removeMemberCallback = removeMemberCallback;
        this.context = new WeakReference<>(context);
        this.channelService = ChannelService.getInstance(context);
        this.index = index;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            if (!TextUtils.isEmpty(userId) && userId.trim().length() != 0 && channelKey != null) {
                removeResponse = channelService.removeMemberFromChannelProcess(channelKey, userId.trim());
                if (!TextUtils.isEmpty(removeResponse)) {
                    return MobiComKitConstants.SUCCESS.equals(removeResponse);
                }
            } else {
                throw new Exception(ApplozicService.getContext(context.get()).getString(R.string.applozic_userId_error_info_in_logs));
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

        if (resultBoolean && removeMemberCallback != null) {
            removeMemberCallback.onSuccess(removeResponse, index);
        } else if (!resultBoolean && exception != null && removeMemberCallback != null) {
            removeMemberCallback.onFailure(removeResponse, exception);
        }
    }
}
