package kommunicate.io.sample;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.uiwidgets.uilistener.KmActionCallback;

import java.util.ArrayList;
import java.util.List;

import io.kommunicate.KmHelper;
import io.kommunicate.Kommunicate;


/**
 * Created by ashish on 23/01/18.
 */

public class KommunicateApplication extends MultiDexApplication implements KmActionCallback {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onReceive(final Context context, final Object object, String action) {

        switch (action) {
            case Kommunicate.START_NEW_CHAT:
                try {
                    KmHelper.setStartNewChat(context, null, null, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case Kommunicate.LOGOUT_CALL:
                KmHelper.performLogout(context, object); //object will receive the exit Activity, the one that will be launched when logout is successfull
                break;

            case Kommunicate.PRECHAT_LOGIN_CALL:
                KmHelper.performLogin(context, (User) object);
                break;
        }
    }
}
