package io.kommunicate.callbacks;

public interface KmRemoveMemberCallback {
    void onSuccess(String response, int index);

    void onFailure(String response, Exception e);
}
