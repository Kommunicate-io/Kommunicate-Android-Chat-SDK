package io.kommunicate.devkit.api.account.user;

import android.content.Context;

import io.kommunicate.devkit.listners.LogoutHandler;
import io.kommunicate.commons.task.CoreAsyncTask;

import java.lang.ref.WeakReference;

import annotations.CleanUpRequired;

@Deprecated
@CleanUpRequired(reason = "Migrated UserLogoutTask to UserLogoutUseCase")
public class UserLogoutTask extends CoreAsyncTask<Void, Boolean> {

    private TaskListener taskListener;
    private final WeakReference<Context> context;
    UserClientService userClientService;
    private Exception mException;
    private LogoutHandler logoutHandler;

    public UserLogoutTask(TaskListener listener, Context context) {
        this.taskListener = listener;
        this.context = new WeakReference<Context>(context);
        userClientService = new UserClientService(context);
    }

    public UserLogoutTask(LogoutHandler listener, Context context) {
        this.logoutHandler = listener;
        this.context = new WeakReference<Context>(context);
        userClientService = new UserClientService(context);
    }

    @Override
    protected Boolean doInBackground() {
        try {
            userClientService.logout();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            mException = e;
            return false;
        }
    }

    @Override
    protected void onPostExecute(final Boolean result) {
        if (taskListener != null) {
            if (result) {
                taskListener.onSuccess(context.get());
            } else {
                taskListener.onFailure(mException);
            }
        }
        if (logoutHandler != null) {
            if (result) {
                logoutHandler.onSuccess(context.get());
            } else {
                logoutHandler.onFailure(mException);
            }
        }
    }

    public interface TaskListener {
        void onSuccess(Context context);

        void onFailure(Exception exception);
    }
}