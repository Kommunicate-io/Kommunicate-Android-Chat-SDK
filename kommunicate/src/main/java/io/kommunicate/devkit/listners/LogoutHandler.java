package io.kommunicate.devkit.listners;

import android.content.Context;

/**
 * Created by reytum on 30/11/17.
 */

public interface LogoutHandler {
    void onSuccess(Context context);

    void onFailure(Exception exception);
}
