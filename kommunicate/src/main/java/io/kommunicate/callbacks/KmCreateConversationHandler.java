package io.kommunicate.callbacks;

import android.content.Context;

import io.kommunicate.models.KmConversationResponse;

/**
 * Created by ashish on 08/03/18.
 */

public interface KmCreateConversationHandler {

    void onSuccess(Context context, KmConversationResponse response);

    void onFailure(Context context, Exception e, String error);
}
