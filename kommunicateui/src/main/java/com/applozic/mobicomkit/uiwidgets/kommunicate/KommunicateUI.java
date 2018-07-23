package com.applozic.mobicomkit.uiwidgets.kommunicate;

import android.content.Context;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.uiwidgets.kommunicate.asyncs.KmAwayMessageTask;
import com.applozic.mobicomkit.uiwidgets.kommunicate.callbacks.KmAwayMessageHandler;

/**
 * Created by ashish on 03/04/18.
 */

public class KommunicateUI {

    public static void getAwayMessage(Context context, String appKey, Integer groupId, KmAwayMessageHandler handler) {
        new KmAwayMessageTask(context, appKey, groupId, handler).execute();
    }

    public static void getAwayMessage(Context context, Integer groupId, KmAwayMessageHandler handler) {
        getAwayMessage(context, Applozic.getInstance(context).getApplicationKey(), groupId, handler);
    }
}
