package io.kommunicate.callbacks;

import android.os.ResultReceiver;

import io.kommunicate.users.KMUser;

public interface KmPrechatCallback {
    void onReceive(KMUser user, ResultReceiver finishActivityReceiver);
}
