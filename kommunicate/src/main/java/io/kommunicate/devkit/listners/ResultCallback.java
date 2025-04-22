package io.kommunicate.devkit.listners;

public interface ResultCallback {
    void onSuccess(Object response);

    void onError(Object error);
}
