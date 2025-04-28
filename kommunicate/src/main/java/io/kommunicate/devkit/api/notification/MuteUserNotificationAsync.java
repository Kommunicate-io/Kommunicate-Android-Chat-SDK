package io.kommunicate.devkit.api.notification;

import android.content.Context;

import io.kommunicate.devkit.api.account.user.UserService;
import io.kommunicate.devkit.feed.ApiResponse;
import io.kommunicate.devkit.feed.ErrorResponseFeed;
import io.kommunicate.commons.json.GsonUtils;
import io.kommunicate.commons.task.CoreAsyncTask;

import java.lang.ref.WeakReference;

import annotations.CleanUpRequired;
import io.kommunicate.R;


/**
 * Created by reytum on 20/11/17.
 */
@Deprecated
@CleanUpRequired(reason = "Migrated MuteUserNotificationAsync to MuteUserNotificationUseCase")
public class MuteUserNotificationAsync extends CoreAsyncTask<Void, ApiResponse> {
    private static final String SUCCESS = "success";
    TaskListener listener;
    Long notificationAfterTime;
    WeakReference<Context> context;
    String userId;
    ApiResponse response;

    public MuteUserNotificationAsync(TaskListener listener, Long notificationAfterTime, String userId, Context context) {
        this.listener = listener;
        this.notificationAfterTime = notificationAfterTime;
        this.userId = userId;
        this.context = new WeakReference<Context>(context);
    }

    @Override
    protected ApiResponse doInBackground() {
        return UserService.getInstance(context.get()).muteUserNotifications(userId, notificationAfterTime);
    }

    @Override
    protected void onPostExecute(ApiResponse apiResponse) {
        super.onPostExecute(apiResponse);

        if (apiResponse == null) {
            listener.onFailure(context.get().getString(R.string.mute_err), context.get());
        } else {
            if (SUCCESS.equals(apiResponse.getStatus())) {
                listener.onSuccess(context.get().getString(R.string.mute_notification), context.get());
            } else {
                if (apiResponse.getErrorResponse() != null) {
                    listener.onFailure(GsonUtils.getJsonFromObject(apiResponse.getErrorResponse().toArray(new ErrorResponseFeed[apiResponse.getErrorResponse().size()]), ErrorResponseFeed[].class), context.get());
                } else {
                    listener.onFailure(context.get().getString(R.string.mute_err), context.get());
                }
            }
        }
    }

    public interface TaskListener {

        void onSuccess(String status, Context context);

        void onFailure(String error, Context context);
    }
}
