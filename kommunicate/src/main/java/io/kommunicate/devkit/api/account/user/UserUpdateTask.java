package io.kommunicate.devkit.api.account.user;

import android.content.Context;

import io.kommunicate.devkit.feed.ApiResponse;
import io.kommunicate.devkit.listners.ResultCallback;
import io.kommunicate.commons.task.CoreAsyncTask;

import java.lang.ref.WeakReference;

import annotations.CleanUpRequired;

@Deprecated
@CleanUpRequired(reason = "Migrated AlUserUpdateTask to UserUpdateUseCase")
public class UserUpdateTask extends CoreAsyncTask<Void, ApiResponse> {
    private WeakReference<Context> context;
    private User user;
    private ResultCallback callback;
    private boolean isForEmail;
    private static final String error_msg = "error";

    public UserUpdateTask(Context context, User user, boolean isForEmail, ResultCallback callback) {
        this.context = new WeakReference<>(context);
        this.user = user;
        this.isForEmail = isForEmail;
        this.callback = callback;
    }

    public UserUpdateTask(Context context, User user, ResultCallback callback) {
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
                callback.onError(error_msg);
            }
        }
    }
}
