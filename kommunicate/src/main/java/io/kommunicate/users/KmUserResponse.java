package io.kommunicate.users;

import io.kommunicate.devkit.feed.ErrorResponseFeed;

import java.util.List;

/**
 * Created by ashish on 30/01/18.
 */

public class KmUserResponse {

    private List<KmContact> contactList;
    private List<ErrorResponseFeed> errorList;
    private Exception exception;
    private boolean isSuccess;

    public List<KmContact> getContactList() {
        return contactList;
    }

    public void setContactList(List<KmContact> contactList) {
        this.contactList = contactList;
    }

    public List<ErrorResponseFeed> getErrorList() {
        return errorList;
    }

    public void setErrorList(List<ErrorResponseFeed> errorList) {
        this.errorList = errorList;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }
}
