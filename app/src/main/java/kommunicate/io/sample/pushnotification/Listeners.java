package kommunicate.io.sample.pushnotification;

import android.util.Log;

import com.applozic.mobicomkit.broadcast.AlEventManager;

import java.util.logging.Logger;

import io.kommunicate.callbacks.KmPluginEventListener;

public class Listeners implements KmPluginEventListener {

    public void register() {
        Log.e("listener", "registered");
        AlEventManager.getInstance().registerPluginEventListener(this);
    }

    public void unregiste() {
        AlEventManager.getInstance().unregisterPluginEventListener();
    }
    @Override
    public void onPluginLaunch() {
        Log.e("listener", "onPluginLaunch");
    }

    @Override
    public void onPluginDismiss() {
        Log.e("listener", "onPluginDismiss");
    }

    @Override
    public void onConversationResolved(Integer conversationId) {
        Log.e("listener", "onConversationResolved : " + conversationId);
    }

    @Override
    public void onConversationRestarted(Integer conversationId) {
        Log.e("listener", "onConversationRestarted : " + conversationId);
    }

    @Override
    public void onRichMessageButtonClick(Integer conversationId, String actionType, Object action) {
        Log.e("listener", "onRichMessageButtonClick : " + conversationId + "actiontype :" + actionType + "Action:" + action);
    }
}
