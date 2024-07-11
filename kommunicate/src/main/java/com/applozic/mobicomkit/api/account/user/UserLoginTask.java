package com.applozic.mobicomkit.api.account.user;

/**
 * Created by Aman on 7/12/2015.
 */

import android.content.Context;

import com.applozic.mobicomkit.api.account.register.RegisterUserClientService;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.listners.AlLoginHandler;
import com.applozic.mobicommons.task.AlAsyncTask;

import java.lang.ref.WeakReference;

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class UserLoginTask extends AlAsyncTask<Void, Boolean> {

    private TaskListener taskListener;
    private final WeakReference<Context> context;
    private User user;
    private Exception mException;
    private RegistrationResponse registrationResponse;
    private UserClientService userClientService;
    private RegisterUserClientService registerUserClientService;
    private AlLoginHandler loginHandler;

    public UserLoginTask(User user, TaskListener listener, Context context) {
        this.taskListener = listener;
        this.context = new WeakReference<Context>(context);
        this.user = user;
        this.userClientService = new UserClientService(context);
        this.registerUserClientService = new RegisterUserClientService(context);
    }

    public UserLoginTask(User user, AlLoginHandler listener, Context context) {
        this.loginHandler = listener;
        this.context = new WeakReference<Context>(context);
        this.user = user;
        this.userClientService = new UserClientService(context);
        this.registerUserClientService = new RegisterUserClientService(context);
    }

    @Override
    protected Boolean doInBackground() {
        try {
            userClientService.clearDataAndPreference();
            registrationResponse = registerUserClientService.createAccount(user);
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
                    taskListener.onSuccess(registrationResponse, context.get());
                } else {
                    taskListener.onFailure(registrationResponse, mException);
                }
            } else {
                taskListener.onFailure(null, mException);
            }
        }

        if (loginHandler != null) {
            if (registrationResponse != null) {
                if (registrationResponse.isRegistrationSuccess()) {
                    loginHandler.onSuccess(registrationResponse, context.get());
                } else {
                    loginHandler.onFailure(registrationResponse, mException);
                }
            } else {
                loginHandler.onFailure(null, mException);
            }
        }
    }

    public interface TaskListener {
        void onSuccess(RegistrationResponse registrationResponse, Context context);

        void onFailure(RegistrationResponse registrationResponse, Exception exception);

    }


}