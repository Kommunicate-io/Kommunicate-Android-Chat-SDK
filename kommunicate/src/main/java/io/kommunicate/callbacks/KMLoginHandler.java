package io.kommunicate.callbacks;

import android.content.Context;

import com.applozic.mobicomkit.listners.AlLoginHandler;

import io.kommunicate.users.KMUser;

/**
 * Created by ashish on 23/01/18.
 */

public interface KMLoginHandler extends AlLoginHandler {
   void onConnected(Context context, KMUser user);
}
