package io.kommunicate.devkit.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Process;

import io.kommunicate.devkit.api.account.user.MobiComUserPreference;
import io.kommunicate.devkit.api.conversation.ChatIntentService;
import io.kommunicate.commons.commons.core.utils.DateUtils;
import io.kommunicate.commons.commons.core.utils.Utils;

/**
 * Created by adarsh on 28/7/15.
 */
public class TimeChangeBroadcastReceiver extends BroadcastReceiver {
    private static final String android_TIME_SET = "android.intent.action.TIME_SET";
    private static final String android_TIMEZONE_CHANGED = "android.intent.action.TIMEZONE_CHANGED";

    @Override
    public void onReceive(final Context context, Intent intent) {

        if (android_TIME_SET.equals(intent.getAction()) || android_TIMEZONE_CHANGED.equals(intent.getAction())) {
            if (!Utils.isAutomaticTimeEnabled(context, android_TIMEZONE_CHANGED.equals(intent.getAction()))) {
                if (Utils.isDeviceInIdleState(context)) {
                    Thread timeChangeThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.printLog(context, "TimeChange", "This thread has been called on date change");
                            long diff = DateUtils.getTimeDiffFromUtc();
                            MobiComUserPreference.getInstance(context).setDeviceTimeOffset(diff);
                        }
                    });
                    timeChangeThread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    timeChangeThread.start();
                } else {
                    Intent applozicIntent = new Intent(context, ChatIntentService.class);
                    applozicIntent.putExtra(ChatIntentService.AL_TIME_CHANGE_RECEIVER, true);
                    ChatIntentService.enqueueWork(context, applozicIntent);
                }
            }
        }
    }
}
