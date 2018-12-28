package io.kommunicate.async;

import android.content.Context;

import com.applozic.mobicomkit.api.account.register.RegisterUserClientService;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.UserClientService;
import com.applozic.mobicomkit.api.account.user.UserLoginTask;
import com.applozic.mobicomkit.api.account.user.UserService;
import com.applozic.mobicomkit.listners.AlLoginHandler;

import java.lang.ref.WeakReference;

import io.kommunicate.services.KmUserClientService;
import io.kommunicate.services.KmUserService;
import io.kommunicate.users.KMUser;

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
    private KmUserService userService;
    private boolean isAgent;


    public KmUserLoginTask(KMUser user, boolean isAgent, AlLoginHandler listener, Context context) {
        super(user, listener, context);
        this.user = user;
        this.context = new WeakReference<Context>(context);
        handler = listener;
        userClientService = new KmUserClientService(this.context.get());
        userService = new KmUserService(this.context.get());
        this.isAgent = isAgent;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            if (isAgent) {
                userClientService.clearDataAndPreference();
                response = userClientService.loginKmUser(user);
            } else {
                new UserClientService(context.get()).clearDataAndPreference();
                response = new RegisterUserClientService(context.get()).createAccount(user);
                UserService.getInstance(context.get()).processPackageDetail();
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
