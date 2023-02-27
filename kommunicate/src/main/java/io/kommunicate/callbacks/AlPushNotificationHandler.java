package io.kommunicate.callbacks;

import io.kommunicate.data.account.register.RegistrationResponse;

/**
 * Created by reytum on 30/11/17.
 */

public interface AlPushNotificationHandler {
    void onSuccess(RegistrationResponse registrationResponse);

    void onFailure(RegistrationResponse registrationResponse, Exception exception);
}
