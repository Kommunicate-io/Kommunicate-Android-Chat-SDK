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
    private static final String err_msg = "Some error occurred";
    private static final String SUCCESS = "success";
    TaskListener listener;
    Long notificationAfterTime;
    WeakReference<Context> context;
    String userId;
    ApiResponse response;
    private static final String success_msg = "Successfully muted/unmuted user";

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
            listener.onFailure(err_msg, context.get());
        } else {
            if (SUCCESS.equals(apiResponse.getStatus())) {
                listener.onSuccess(success_msg, context.get());
            } else {
                if (apiResponse.getErrorResponse() != null) {
                    listener.onFailure(GsonUtils.getJsonFromObject(apiResponse.getErrorResponse().toArray(new ErrorResponseFeed[apiResponse.getErrorResponse().size()]), ErrorResponseFeed[].class), context.get());
                } else {
                    listener.onFailure(err_msg, context.get());
                }
            }
        }
    }

    public interface TaskListener {

        void onSuccess(String status, Context context);

        void onFailure(String error, Context context);
    }
}
