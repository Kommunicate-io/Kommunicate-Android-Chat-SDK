package io.kommunicate.devkit.listners;

import android.app.Activity;
import android.net.Uri;

public interface AttachmentFilteringListener {
    void onAttachmentSelected(Activity activity, Uri selectedFileUri, ResultCallback callback);
}
