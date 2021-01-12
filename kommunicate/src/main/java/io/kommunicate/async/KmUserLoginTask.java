package io.kommunicate.async;

import android.content.Context;
import android.os.ResultReceiver;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.api.account.register.RegisterUserClientService;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.api.account.user.UserClientService;
import com.applozic.mobicomkit.api.account.user.UserLoginTask;
import com.applozic.mobicomkit.listners.AlLoginHandler;

import java.lang.ref.WeakReference;

import io.kommunicate.services.KmUserClientService;
import io.kommunicate.users.KMUser;
import io.kommunicate.utils.KmConstants;
import io.kommunicate.utils.KmAppSettingPreferences;

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
                KmAppSettingPreferences.fetchAppSetting(context.get(), Applozic.getInstance(context.get()).getApplicationKey());
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
                } else {
                    handler.onFailure(response, e);
                }
            }
        } else {
            if (handler != null) {
                handler.onFailure(null, e);
            }
        }
    }
}
