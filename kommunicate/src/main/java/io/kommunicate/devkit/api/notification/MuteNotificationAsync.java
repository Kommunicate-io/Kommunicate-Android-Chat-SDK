package io.kommunicate.devkit.api.notification;

import android.content.Context;

import io.kommunicate.devkit.channel.service.ChannelService;
import io.kommunicate.devkit.feed.ApiResponse;
import io.kommunicate.commons.task.CoreAsyncTask;

import annotations.CleanUpRequired;

/**
 * Created by Adarsh on 12/30/16.
 */
@Deprecated
@CleanUpRequired(reason = "Migrated MuteNotificationAsync to MuteNotificationUseCase")
public class MuteNotificationAsync extends CoreAsyncTask<Void, Boolean> {

    private final MuteNotificationAsync.TaskListener taskListener;
    private final Context context;
    private ApiResponse apiResponse;
    private Exception mException;
    private MuteNotificationRequest muteNotificationRequest;

    public MuteNotificationAsync(Context context, MuteNotificationAsync.TaskListener listener, MuteNotificationRequest request) {
        this.context = context;
        this.taskListener = listener;
        this.muteNotificationRequest = request;
    }

    @Override
    protected Boolean doInBackground() {
        try {
            apiResponse = ChannelService.getInstance(context).muteNotifications(muteNotificationRequest);
        } catch (Exception e) {
            e.printStackTrace();
            mException = e;
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean result) {
        if (result && this.taskListener != null) {
            this.taskListener.onSuccess(apiResponse);
        } else if (mException != null && this.taskListener != null) {
            this.taskListener.onFailure(apiResponse, mException);
        }
        this.taskListener.onCompletion();
    }

    public interface TaskListener {

        void onSuccess(ApiResponse apiResponse);

        void onFailure(ApiResponse apiResponse, Exception exception);

        void onCompletion();
    }


}
