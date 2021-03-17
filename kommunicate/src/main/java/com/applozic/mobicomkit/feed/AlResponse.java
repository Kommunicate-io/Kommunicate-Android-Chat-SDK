package com.applozic.mobicomkit.feed;

public class AlResponse {

    public static final String SUCCESS = "success";
    public static final String ERROR = "error";
    private String status;
    private Object response;
    private Exception exception;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }

    public boolean isSuccess() {
        return SUCCESS.equals(status);
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}