package io.kommunicate.devkit.api.authentication;

import android.content.Context;

import io.kommunicate.devkit.api.MobiComKitClientService;
import io.kommunicate.devkit.api.account.register.RegisterUserClientService;
import io.kommunicate.devkit.api.account.user.MobiComUserPreference;
import io.kommunicate.devkit.listners.ResultCallback;
import io.kommunicate.commons.task.CoreAsyncTask;

import java.lang.ref.WeakReference;

import annotations.CleanUpRequired;

@Deprecated
@CleanUpRequired(reason = "Migrated RefreshAuthTokenTask to RefreshAuthTokenUseCase")
public class RefreshAuthTokenTask extends CoreAsyncTask<Void, Boolean> {

    private final String applicationId;
    private final String userId;
    private final WeakReference<Context> context;
    private final ResultCallback callback;

    public RefreshAuthTokenTask(Context context, String applicationId, String userId, ResultCallback callback) {
        this.context = new WeakReference<>(context);
        this.applicationId = applicationId;
        this.userId = userId;
        this.callback = callback;
    }

    public RefreshAuthTokenTask(Context context, ResultCallback callback) {
        this(context, MobiComKitClientService.getApplicationKey(context), MobiComUserPreference.getInstance(context).getUserId(), callback);
    }

    @Override
    protected Boolean doInBackground() {
        return new RegisterUserClientService(context.get()).refreshAuthToken(applicationId, userId);
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (callback != null) {
            if (aBoolean) {
                callback.onSuccess(true);
            } else {
                callback.onError(false);
            }
        }
    }
}
