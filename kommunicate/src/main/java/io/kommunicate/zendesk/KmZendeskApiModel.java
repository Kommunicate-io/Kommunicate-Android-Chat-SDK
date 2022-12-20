package io.kommunicate.zendesk;

public class KmZendeskApiModel {
    private static final String SUCCESS = "SUCCESS";
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

    public boolean isSuccess() {
        return SUCCESS.equals(code);
    }

    public class JwtData {
        private String jwt;

        public String getJwt() {
            return jwt;
        }
    }
}
