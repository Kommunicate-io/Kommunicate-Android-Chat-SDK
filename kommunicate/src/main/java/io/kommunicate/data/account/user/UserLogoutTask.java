package io.kommunicate.data.account.user;

import android.content.Context;

import java.lang.ref.WeakReference;

import io.kommunicate.callbacks.AlLogoutHandler;
import io.kommunicate.data.async.task.AlAsyncTask;

public class UserLogoutTask extends AlAsyncTask<Void, Boolean> {

    private final WeakReference<Context> context;
    UserClientService userClientService;
    private TaskListener taskListener;
    private Exception mException;
    private AlLogoutHandler logoutHandler;

    public UserLogoutTask(TaskListener listener, Context context) {
        this.taskListener = listener;
        this.context = new WeakReference<Context>(context);
        userClientService = new UserClientService(context);
    }

    public UserLogoutTask(AlLogoutHandler listener, Context context) {
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