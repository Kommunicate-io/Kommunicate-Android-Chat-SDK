package kommunicate.io.sample;

import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.MessageBuilder;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.ALRichMessageListener;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.payment.PaymentActivity;
import com.applozic.mobicomkit.uiwidgets.uilistener.KmActionCallback;

import java.util.ArrayList;
import java.util.List;

import io.kommunicate.KmHelper;
import io.kommunicate.Kommunicate;
import io.kommunicate.app.BuildConfig;

/**
 * Created by ashish on 23/01/18.
 */
public class KommunicateApplication extends MultiDexApplication implements KmActionCallback, ALRichMessageListener {

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
                List<String> agents = new ArrayList<>();
                agents.add(BuildConfig.KOMMUNICATE_AGENT);
                List<String> bots = new ArrayList<>();
                try {
                    KmHelper.setStartNewChat(context, agents, bots);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case Kommunicate.LOGOUT_CALL:
                KmHelper.performLogout(context, object);
                break;
        }
    }

    @Override
    public void onAction(Context context, String action, Message message, Object object) {
        switch (action) {
            case "sendMessage":
                new MessageBuilder(context).setMessage((String) object).setGroupId(message.getGroupId()).send();
                break;

            case "openUrl":
                Intent intent = new Intent(context, PaymentActivity.class);
                intent.putExtra("formAction", (String) object);
                context.startActivity(intent);
                break;

            case "openActivity":
                Intent intent1 = new Intent(context, MainActivity.class);
                context.startActivity(intent1);
        }
    }
}
