package io.kommunicate.devkit.api.account.user;

import android.content.Context;

import io.kommunicate.devkit.api.account.register.RegisterUserClientService;
import io.kommunicate.devkit.api.account.register.RegistrationResponse;
import io.kommunicate.devkit.listners.PushNotificationHandler;
import io.kommunicate.commons.task.CoreAsyncTask;

import java.lang.ref.WeakReference;

import annotations.CleanUpRequired;

/**
 * Created by devashish on 7/22/2015.
 */
@Deprecated
@CleanUpRequired(reason = "Migrated PushNotificationTask to PushNotificationUseCase")
public class PushNotificationTask extends CoreAsyncTask<Void, Boolean> {

    private String pushNotificationId;
    private TaskListener taskListener;
    private WeakReference<Context> context;
    private Exception mException;
    private RegistrationResponse registrationResponse;
    private PushNotificationHandler pushNotificationHandler;

    public PushNotificationTask(String pushNotificationId, TaskListener listener, Context context) {
        this.pushNotificationId = pushNotificationId;
        this.taskListener = listener;
        this.context = new WeakReference<Context>(context);
    }

    public PushNotificationTask(Context context, String pushNotificationId, PushNotificationHandler listener) {
        this.pushNotificationId = pushNotificationId;
        this.pushNotificationHandler = listener;
        this.context = new WeakReference<Context>(context);
    }

    @Override
    protected Boolean doInBackground() {
        try {
            registrationResponse = new RegisterUserClientService(context.get()).updatePushNotificationId(pushNotificationId);
        } catch (Exception e) {
            e.printStackTrace();
            mException = e;
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean result) {
        // And if it is we call the callback function on it.
        if (taskListener != null) {
            if (registrationResponse != null) {
                if (registrationResponse.isRegistrationSuccess()) {
                    taskListener.onSuccess(registrationResponse);
                } else {
                    taskListener.onFailure(registrationResponse, mException);
                }
            } else {
                taskListener.onFailure(null, mException);
            }
        }

        if (pushNotificationHandler != null) {
            if (registrationResponse != null) {
                if (registrationResponse.isRegistrationSuccess()) {
                    pushNotificationHandler.onSuccess(registrationResponse);
                } else {
                    pushNotificationHandler.onFailure(registrationResponse, mException);
                }
            } else {
                pushNotificationHandler.onFailure(null, mException);
            }
        }
    }

    public interface TaskListener {

        void onSuccess(RegistrationResponse registrationResponse);

        void onFailure(RegistrationResponse registrationResponse, Exception exception);

    }
}