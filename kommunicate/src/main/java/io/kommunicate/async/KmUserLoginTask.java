package io.kommunicate.async;

import android.content.Context;

import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.api.account.user.UserLoginTask;
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


    public KmUserLoginTask(KMUser user, AlLoginHandler listener, Context context) {
        super(user, listener, context);
        this.user = user;
        this.context = new WeakReference<Context>(context);
        handler = listener;
        userClientService = new KmUserClientService(this.context.get());
        userService = new KmUserService(this.context.get());
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            userClientService.clearDataAndPreference();
            response = userClientService.loginKmUser(user);
        } catch (Exception e) {
            e.printStackTrace();
            this.e = e;
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result && handler != null) {
            handler.onSuccess(response, context.get());
        } else if (handler != null && e != null) {
            handler.onFailure(response, e);
        }
    }
}
