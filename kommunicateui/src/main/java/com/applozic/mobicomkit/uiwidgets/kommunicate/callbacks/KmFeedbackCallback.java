package com.applozic.mobicomkit.uiwidgets.kommunicate.callbacks;

import android.content.Context;

import com.applozic.mobicomkit.uiwidgets.kommunicate.models.KmApiResponse;
import com.applozic.mobicomkit.uiwidgets.kommunicate.models.KmFeedback;

/**
 * callback for the feedback api async task
 * @author shubham
 * @date 25/July/2019
 */
public interface KmFeedbackCallback {
    void onSuccess(Context context, KmApiResponse<KmFeedback> response);
    void onFailure(Context context, Exception e, String response);
}
