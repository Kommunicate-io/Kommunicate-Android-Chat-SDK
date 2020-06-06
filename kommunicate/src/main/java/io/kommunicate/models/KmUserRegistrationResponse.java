package io.kommunicate.models;

import com.applozic.mobicomkit.api.account.register.RegistrationResponse;

//TODO: won't be needed after applozic sdk update
public class KmUserRegistrationResponse extends RegistrationResponse {
    private String authToken;

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
}
