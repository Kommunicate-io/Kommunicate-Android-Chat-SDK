package io.kommunicate.callbacks;

import android.app.Activity;
import android.net.Uri;

public interface AttachmentFilteringListener {
    void onAttachmentSelected(Activity activity, Uri selectedFileUri, AlCallback callback);
}
