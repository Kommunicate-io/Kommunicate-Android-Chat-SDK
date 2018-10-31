package io.kommunicate.callbacks;

public interface KmCallback {
    void onSuccess(Object message);

    void onFailure(Object error);
}
