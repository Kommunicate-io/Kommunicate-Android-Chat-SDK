package io.kommunicate.ui.uilistener;

public interface KmStoragePermissionListener {
    boolean isPermissionGranted();

    void checkPermission(KmStoragePermission storagePermission);
}
