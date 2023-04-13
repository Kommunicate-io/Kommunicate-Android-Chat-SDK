package io.kommunicate.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Process;

import io.kommunicate.data.account.user.MobiComUserPreference;
import io.kommunicate.data.conversation.KmIntentService;
import io.kommunicate.utils.DateUtils;
import io.kommunicate.utils.Utils;

/**
 * Created by adarsh on 28/7/15.
 */
public class TimeChangeBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {

        if ("android.intent.action.TIME_SET".equals(intent.getAction()) || "android.intent.action.TIMEZONE_CHANGED".equals(intent.getAction())) {
            if (!Utils.isAutomaticTimeEnabled(context, "android.intent.action.TIMEZONE_CHANGED".equals(intent.getAction()))) {
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
                    Intent kmIntent = new Intent(context, KmIntentService.class);
                    kmIntent.putExtra(KmIntentService.AL_TIME_CHANGE_RECEIVER, true);
                    KmIntentService.enqueueWork(context, kmIntent);
                }
            }
        }
    }
}
