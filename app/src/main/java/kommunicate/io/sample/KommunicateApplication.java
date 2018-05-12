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

import io.kommunicate.KmException;
import io.kommunicate.Kommunicate;


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
                //Kommunicate.startOrGetConversation(context, "testClientGroupId", "reytum@live.com", null, "My Group");
                //Kommunicate.setStartNewChat(context, "vipin+testkm01012018@applozic.com", "Hotel-Booking-Assistant"); //pass null if you want to use default bot
                List<String> agents = new ArrayList<>();
                agents.add("reytum@live.com");
                List<String> bots = new ArrayList<>();
                //Kommunicate.setStartNewChat(context, "reytum@live.com", "Hotel-Booking-Assistant");
                try {
                    Kommunicate.startNewConversation(context, null, agents, bots,false, null);
                } catch (KmException e) {
                    e.printStackTrace();
                }
                //Kommunicate.setStartNewUniqueChat(context, agents, bots);
                break;

            case Kommunicate.LOGOUT_CALL:
                Kommunicate.performLogout(context, object); //object will receive the exit Activity, the one that will be launched when logout is successfull
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
