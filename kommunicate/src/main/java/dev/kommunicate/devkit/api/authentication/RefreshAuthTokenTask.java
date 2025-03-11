package dev.kommunicate.devkit.api.authentication;

import android.content.Context;

import dev.kommunicate.devkit.api.MobiComKitClientService;
import dev.kommunicate.devkit.api.account.register.RegisterUserClientService;
import dev.kommunicate.devkit.api.account.user.MobiComUserPreference;
import dev.kommunicate.devkit.listners.AlCallback;
import dev.kommunicate.commons.task.AlAsyncTask;

import java.lang.ref.WeakReference;

import annotations.CleanUpRequired;

@Deprecated
@CleanUpRequired(reason = "Migrated RefreshAuthTokenTask to RefreshAuthTokenUseCase")
public class RefreshAuthTokenTask extends AlAsyncTask<Void, Boolean> {

    private final String applicationId;
    private final String userId;
    private final WeakReference<Context> context;
    private final AlCallback callback;

    public RefreshAuthTokenTask(Context context, String applicationId, String userId, AlCallback callback) {
        this.context = new WeakReference<>(context);
        this.applicationId = applicationId;
        this.userId = userId;
        this.callback = callback;
    }

    public RefreshAuthTokenTask(Context context, AlCallback callback) {
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
