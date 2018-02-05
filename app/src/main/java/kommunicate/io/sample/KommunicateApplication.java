package kommunicate.io.sample;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import com.applozic.mobicomkit.uiwidgets.uilistener.KmActionCallback;
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
    public void onReceive(Context context, final Object object, String action) {

        switch (action) {
            case Kommunicate.START_NEW_CHAT:
                MainActivity.setStartNewChat(context, "reytum@live.com", null); //pass null if you want to use default bot
                break;

            case Kommunicate.LOGOUT_CALL:
                MainActivity.performLogout(context, object); //object will receive the exit Activity, the one that will be launched when logout is successfull
                break;
        }
    }
}
