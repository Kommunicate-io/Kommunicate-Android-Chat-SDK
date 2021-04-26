package com.applozic.mobicomkit.api.account.user;

import android.content.Context;

import com.applozic.mobicomkit.api.account.register.RegisterUserClientService;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.listners.AlPushNotificationHandler;
import com.applozic.mobicommons.task.AlAsyncTask;

import java.lang.ref.WeakReference;

/**
 * Created by devashish on 7/22/2015.
 */
public class PushNotificationTask extends AlAsyncTask<Void, Boolean> {

    private String pushNotificationId;
    private TaskListener taskListener;
    private WeakReference<Context> context;
    private Exception mException;
    private RegistrationResponse registrationResponse;
    private AlPushNotificationHandler pushNotificationHandler;

    public PushNotificationTask(String pushNotificationId, TaskListener listener, Context context) {
        this.pushNotificationId = pushNotificationId;
        this.taskListener = listener;
        this.context = new WeakReference<Context>(context);
    }

    public PushNotificationTask(Context context, String pushNotificationId, AlPushNotificationHandler listener) {
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