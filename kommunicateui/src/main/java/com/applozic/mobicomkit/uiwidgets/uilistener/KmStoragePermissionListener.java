package com.applozic.mobicomkit.uiwidgets.uilistener;

public interface KmStoragePermissionListener {
    boolean isPermissionGranted();

    void checkPermission(KmStoragePermission storagePermission);
}
