package io.kommunicate.callbacks;

import android.content.Context;

import annotations.CleanUpRequired;

/**
 * Created by ashish on 21/04/18.
 */
@Deprecated
@CleanUpRequired(reason = "move to the Task Listener instead")
public interface KmFaqTaskListener {
    void onSuccess(Context context, Object object);

    void onFailure(Context context, Exception exception, Object object);
}
