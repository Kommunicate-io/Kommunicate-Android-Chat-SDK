package io.kommunicate.data.api.authentication;

import android.content.Context;

import java.lang.ref.WeakReference;

import io.kommunicate.callbacks.AlCallback;
import io.kommunicate.data.api.MobiComKitClientService;
import io.kommunicate.data.account.register.RegisterUserClientService;
import io.kommunicate.data.account.user.MobiComUserPreference;
import io.kommunicate.data.async.task.AlAsyncTask;

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
