package com.applozic.mobicomkit.feed;
/**
 * @author shanki-091
 */
import java.io.Serializable;

/**
 * Class to be serialized to response
 */
public class ResultFeed implements Serializable {
    private int code;
    private String message;
    private String reason;
    private String detail;
    private static final String success = "Success";
    private static final String MESSAGE_SUCCESS = "rest.API.message.success";
    private static final String API_BAD_REQUEST = "rest.API.message.bad.request";
    private static final String API_MSG_FORBIDDEN = "rest.API.message.forbidden";
    private static final String API_FIELD_NOT_FOUND = "rest.API.message.required.field.not.found";
    private static final String API_AUTH_REQUIRED = "rest.API.message.authentication.required";
    private static final String API_NOT_AUTHORIZED = "rest.API.message.not.authorized";
    private static final String APIREQUEST_ALREADY_PROCESSED = "rest.API.message.request.already.processed";
    private static final String API_MSG_INCORRECT = "rest.API.message.incorrect.required.field";
    private static final String API_ACCOUNT_INACTIVE = "rest.API.message.account.inactive";
    private static final String API_ACCOUNT_DAILY_LIMIT = "rest.API.message.account.over.daily.limit";
    private static final String API_MSG_NOTFOUND = "rest.API.message.not.found";
    private static final String API_REQUEST_TIMEOUT = "rest.API.message.request.timeout";
    private static final String API_MSG_INTERNAL_ERROR = "rest.API.message.internal.server.error";
    private static final String API_SERVICE_UNAVAILABLE = "rest.API.message.service.unavailable";


    public ResultFeed() {
        this(RestAPICodes.SUCCESS);
    }

    public ResultFeed(RestAPICodes apiCode) {
        this.code = apiCode.code;
        this.message = apiCode.message;
        this.reason = apiCode.reason;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setRestAPICode(RestAPICodes apiCode) {
        this.code = apiCode.code;
        this.message = apiCode.message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public enum RestAPICodes {

        SUCCESS(200, success, MESSAGE_SUCCESS), BAD_REQUEST(400, "Bad Request", API_BAD_REQUEST), FORBIDDEN(
                403, "Forbidden", API_MSG_FORBIDDEN), FIELD_REQUIRED(405, "Field Required", API_FIELD_NOT_FOUND), AUTHENTICATION_REQUIRED(401, "Authentication Required", API_AUTH_REQUIRED),
        NOT_AUTHORIZED(401, "Not Authorized",
                API_NOT_AUTHORIZED), REQUEST_ALREADY_PROCESSED(
                406, "Request Already Processed", APIREQUEST_ALREADY_PROCESSED), INCOREECT_REQUIRED_FIELD(
                407, "Incorrect Required Field", API_MSG_INCORRECT), ACCOUNT_INACTIVE(403, "Account Inactive",
                API_ACCOUNT_INACTIVE), DAILY_LIMIT_OVER(429, "Account Over Rate Limit",
                API_ACCOUNT_DAILY_LIMIT), NOT_FOUND(404, "Not Found", API_MSG_NOTFOUND), REQUEST_TIMEOUT(
                408, "Request Timeout", API_REQUEST_TIMEOUT), INTERNAL_SERVER_ERROR(500,
                "Internal Server Error", API_MSG_INTERNAL_ERROR), SERVICE_UNAVAILABLE(503,
                "Service Unavailable", API_SERVICE_UNAVAILABLE);

        public final int code;
        public final String message;
        public final String reason;

        RestAPICodes(int code, String message, String reason) {
            this.code = code;
            this.message = message;
            this.reason = reason;
        }
    }
}
