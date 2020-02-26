package io.kommunicate.callbacks;

import android.content.Context;
import android.os.ResultReceiver;

public interface KmPrechatCallback<T> {
    void onReceive(T data, Context context, ResultReceiver finishActivityReceiver);

    void onError(String error);
}
