package io.kommunicate.zendesk;

public class KmZendeskJwtModel {
    private String code;
    private String message;
    private JwtData data;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public JwtData getData() {
        return data;
    }

    public class JwtData {
        private String jwt;

        public String getJwt() {
            return jwt;
        }
    }
}
