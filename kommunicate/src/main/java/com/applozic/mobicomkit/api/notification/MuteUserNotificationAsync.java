package com.applozic.mobicomkit.api.notification;

import android.content.Context;

import com.applozic.mobicomkit.api.account.user.UserService;
import com.applozic.mobicomkit.feed.ApiResponse;
import com.applozic.mobicomkit.feed.ErrorResponseFeed;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.task.AlAsyncTask;

import java.lang.ref.WeakReference;


/**
 * Created by reytum on 20/11/17.
 */

public class MuteUserNotificationAsync extends AlAsyncTask<Void, ApiResponse> {

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
            listener.onFailure("Some error occurred", context.get());
        } else {
            if ("success".equals(apiResponse.getStatus())) {
                listener.onSuccess("Successfully muted/unmuted user", context.get());
            } else {
                if (apiResponse.getErrorResponse() != null) {
                    listener.onFailure(GsonUtils.getJsonFromObject(apiResponse.getErrorResponse().toArray(new ErrorResponseFeed[apiResponse.getErrorResponse().size()]), ErrorResponseFeed[].class), context.get());
                } else {
                    listener.onFailure("Some unknown error occurred", context.get());
                }
            }
        }
    }

    public interface TaskListener {

        void onSuccess(String status, Context context);

        void onFailure(String error, Context context);
    }
}
