package io.kommunicate.devkit.listners;

import io.kommunicate.devkit.api.account.register.RegistrationResponse;

/**
 * Created by reytum on 30/11/17.
 */

public interface PushNotificationHandler {
    void onSuccess(RegistrationResponse registrationResponse);

    void onFailure(RegistrationResponse registrationResponse, Exception exception);
}
