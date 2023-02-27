package io.kommunicate.uiwidgets.uilistener;

public interface KmStoragePermissionListener {
    boolean isPermissionGranted();

    void checkPermission(KmStoragePermission storagePermission);
}
