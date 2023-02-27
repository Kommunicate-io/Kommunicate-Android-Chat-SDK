package io.kommunicate.data.account.user;

import android.content.Context;

import java.lang.ref.WeakReference;

import io.kommunicate.callbacks.AlCallback;
import io.kommunicate.data.async.task.AlAsyncTask;
import io.kommunicate.models.feed.ApiResponse;

public class AlUserUpdateTask extends AlAsyncTask<Void, ApiResponse> {
    private WeakReference<Context> context;
    private User user;
    private AlCallback callback;
    private boolean isForEmail;

    public AlUserUpdateTask(Context context, User user, boolean isForEmail, AlCallback callback) {
        this.context = new WeakReference<>(context);
        this.user = user;
        this.isForEmail = isForEmail;
        this.callback = callback;
    }

    public AlUserUpdateTask(Context context, User user, AlCallback callback) {
        this(context, user, false, callback);
    }

    @Override
    protected ApiResponse doInBackground() {
        return UserService.getInstance(context.get()).updateUserWithResponse(user, isForEmail);
    }

    @Override
    protected void onPostExecute(ApiResponse apiResponse) {
        super.onPostExecute(apiResponse);
        if (callback != null) {
            if (apiResponse != null) {
                if (apiResponse.isSuccess()) {
                    callback.onSuccess(apiResponse.getResponse());
                } else {
                    callback.onError(apiResponse.getErrorResponse());
                }
            } else {
                callback.onError("error");
            }
        }
    }
}
