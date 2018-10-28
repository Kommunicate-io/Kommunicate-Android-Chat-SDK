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
import io.kommunicate.app.BuildConfig;

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
                //Kommunicate.startOrGetConversation(context, "testClientGroupId", "reytum@live.com", null, "My Group");
                //Kommunicate.setStartNewChat(context, "vipin+testkm01012018@applozic.com", "Hotel-Booking-Assistant"); //pass null if you want to use default bot
                List<String> agents = new ArrayList<>();
                agents.add(BuildConfig.KOMMUNICATE_AGENT);
                List<String> bots = new ArrayList<>();
                //Kommunicate.setStartNewChat(context, "reytum@live.com", "Hotel-Booking-Assistant");
                try {
                    KmHelper.setStartNewChat(context, agents, bots);
                    //Kommunicate.startNewConversation(context, null, agents, bots,false, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //KmHelper.setStartNewUniqueChat(context, agents, bots);
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
