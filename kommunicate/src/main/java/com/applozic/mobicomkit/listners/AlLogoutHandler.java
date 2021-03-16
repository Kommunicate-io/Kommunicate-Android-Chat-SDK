package com.applozic.mobicomkit.listners;

import android.content.Context;

/**
 * Created by reytum on 30/11/17.
 */

public interface AlLogoutHandler {
    void onSuccess(Context context);

    void onFailure(Exception exception);
}
