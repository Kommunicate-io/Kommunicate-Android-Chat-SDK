package io.kommunicate.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import io.kommunicate.data.account.user.MobiComUserPreference;
import io.kommunicate.data.conversation.KmIntentService;
import io.kommunicate.utils.Utils;

/**
 * Created by devashish on 29/08/15.
 */
public class ConnectivityReceiver extends BroadcastReceiver {

    static final private String TAG = "ConnectivityReceiver";
    static final private String CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
    private static final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    private static final String REBOOT_COMPLETED = "android.intent.action.QUICKBOOT_POWERON";
    private static boolean firstConnect = true;
    Context context;

    @Override
    public void onReceive(@NonNull final Context context, @NonNull Intent intent) {
        this.context = context;
        String action = intent.getAction();
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(action));
        Utils.printLog(context, TAG, action);

        if (!MobiComUserPreference.getInstance(context).isLoggedIn()) {
            return;
        }

        if (BOOT_COMPLETED.equalsIgnoreCase(action) || REBOOT_COMPLETED.equalsIgnoreCase(action)) {
            Intent connectivityIntent = new Intent(context, KmIntentService.class);
            connectivityIntent.putExtra(KmIntentService.AL_SYNC_ON_CONNECTIVITY, true);
            KmIntentService.enqueueWork(context, connectivityIntent);
        }

        if (CONNECTIVITY_CHANGE.equalsIgnoreCase(action)) {
            if (!Utils.isInternetAvailable(context)) {
                firstConnect = true;
                return;
            }
            ConnectivityManager cm = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
            if (cm != null) {
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    if (firstConnect) {
                        firstConnect = false;
                        Intent connectivityIntent = new Intent(context, KmIntentService.class);
                        connectivityIntent.putExtra(KmIntentService.AL_SYNC_ON_CONNECTIVITY, true);
                        KmIntentService.enqueueWork(context, connectivityIntent);
                    }
                }
            }
        }
    }
}


