package io.kommunicate.data.async;

import android.content.Context;
import android.os.ResultReceiver;

import java.lang.ref.WeakReference;

import io.kommunicate.KmChat;
import io.kommunicate.callbacks.AlLoginHandler;
import io.kommunicate.data.account.register.RegisterUserClientService;
import io.kommunicate.data.account.register.RegistrationResponse;
import io.kommunicate.data.account.user.UserClientService;
import io.kommunicate.data.account.user.UserLoginTask;
import io.kommunicate.data.services.KmUserClientService;
import io.kommunicate.users.KMUser;
import io.kommunicate.utils.KmAppSettingPreferences;
import io.kommunicate.utils.KmConstants;

/**
 * Created by ashish on 10/02/18.
 */

public class KmUserLoginTask extends UserLoginTask {
    private Exception e;
    private KMUser user;
    private AlLoginHandler handler;
    private WeakReference<Context> context;
    private RegistrationResponse response;
    private KmUserClientService userClientService;
    private boolean isAgent;
    private ResultReceiver prechatReceiver;


    public KmUserLoginTask(KMUser user, boolean isAgent, AlLoginHandler listener, Context context) {
        super(user, listener, context);
        this.user = user;
        this.context = new WeakReference<Context>(context);
        handler = listener;
        userClientService = new KmUserClientService(this.context.get());
        this.isAgent = isAgent;
    }

    public KmUserLoginTask(KMUser user, boolean isAgent, AlLoginHandler listener, Context context, ResultReceiver prechatReceiver) {
        super(user, listener, context);
        this.user = user;
        this.context = new WeakReference<Context>(context);
        handler = listener;
        userClientService = new KmUserClientService(this.context.get());
        this.isAgent = isAgent;
        this.prechatReceiver = prechatReceiver;
    }

    @Override
    protected Boolean doInBackground() {
        try {
            if (isAgent) {
                userClientService.clearDataAndPreference();
                response = userClientService.loginKmUser(user);
            } else {
                new UserClientService(context.get()).clearDataAndPreference();
                KmAppSettingPreferences.fetchAppSetting(context.get(), KmChat.getInstance(context.get()).getApplicationKey());
                response = new RegisterUserClientService(context.get()).createAccount(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.e = e;
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (prechatReceiver != null) {
            prechatReceiver.send(KmConstants.PRECHAT_RESULT_CODE, null);
        }
        if (response != null) {
            if (handler != null) {
                if (response.isRegistrationSuccess()) {
                    handler.onSuccess(response, context.get());
                } else if (e != null) {
                    handler.onFailure(response, e);
                } else {
                    handler.onFailure(response, new Exception(response.getMessage()));
                }
            }
        } else {
            if (handler != null) {
                handler.onFailure(null, e);
            }
        }
    }
}