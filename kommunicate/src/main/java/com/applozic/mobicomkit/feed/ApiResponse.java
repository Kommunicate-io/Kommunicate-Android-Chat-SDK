package com.applozic.mobicomkit.feed;

import com.applozic.mobicommons.json.JsonMarker;

import java.util.List;

/**
 * Created by sunil on 23/12/15.
 */
public class ApiResponse<T> extends JsonMarker {

    private static final String SUCCESS = "success";
    private String status;
    private Long generatedAt;
    private T response;
    private List<ErrorResponseFeed> errorResponse;


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Long generatedAt) {
        this.generatedAt = generatedAt;
    }

    public T getResponse() {
        return response;
    }

    public void setResponse(T response) {
        this.response = response;
    }

    public boolean isSuccess() {
        return SUCCESS.equals(status);
    }

    public List<ErrorResponseFeed> getErrorResponse() {
        return errorResponse;
    }

    public void setErrorResponse(List<ErrorResponseFeed> errorResponse) {
        this.errorResponse = errorResponse;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "status='" + status + '\'' +
                ", generatedAt='" + generatedAt + '\'' +
                ", response='" + response + '\'' +
                '}';
    }
}
