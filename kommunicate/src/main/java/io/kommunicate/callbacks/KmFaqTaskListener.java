package io.kommunicate.callbacks;

import android.content.Context;

/**
 * Created by ashish on 21/04/18.
 */

public interface KmFaqTaskListener {
    void onSucess(Context context, Object object);

    void onFailure(Context context, Exception exception, Object object);
}
