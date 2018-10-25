package io.kommunicate.callbacks;

import io.kommunicate.users.KMUser;

public interface KmPrechatCallback {
    void onReceive(KMUser user);
}
