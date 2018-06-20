package com.applozic.mobicomkit.uiwidgets.kommunicate.callbacks;

import android.content.Context;

import com.applozic.mobicomkit.uiwidgets.kommunicate.models.KmAwayMessageResponse;

/**
 * Created by ashish on 03/04/18.
 */

public interface KmAwayMessageHandler {
    void onSuccess(Context context, KmAwayMessageResponse.KmMessageResponse response);

    void onFailure(Context context, Exception e, String response);
}
