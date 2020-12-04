package com.applozic.mobicomkit.uiwidgets.instruction;

import android.Manifest;
import android.app.Activity;
import com.google.android.material.snackbar.Snackbar;
import android.view.View;
import android.widget.LinearLayout;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.MobicomLocationActivity;
import com.applozic.mobicommons.commons.core.utils.PermissionsUtils;

/**
 * Created by sunil on 22/1/16.
 */
public class KmPermissions {
    private LinearLayout snackBarLayout;
    private Activity activity;

    //CODES: all permission request codes here are between 900 and 999 to avoid conflicts
    //CODES: refer to the PermissionUtils.java request codes to avoid conflicts
    public static final int REQUEST_STORAGE_MULTI_SELECT_GALLERY = 901;
    public static final int REQUEST_STORAGE_ATTACHMENT = 902;
    public static final int REQUEST_CAMERA_PHOTO = 903;
    public static final int REQUEST_CAMERA_VIDEO = 904;

    public KmPermissions(Activity activity, LinearLayout linearLayout) {
        this.activity = activity;
        this.snackBarLayout = linearLayout;
    }

    public void checkRuntimePermissionForStorage() {
        if (PermissionsUtils.checkSelfForStoragePermission(activity)) {
            requestStoragePermissions();
        }
    }

    public void checkRuntimePermissionForLocation() {
        if (PermissionsUtils.checkSelfPermissionForLocation(activity)) {
            requestLocationPermissions();
        } else {
            ((ConversationActivity) activity).processingLocation();
        }
    }

    public void requestStoragePermissions() {
        if (PermissionsUtils.shouldShowRequestForStoragePermission(activity)) {
            showSnackBar(R.string.storage_permission, PermissionsUtils.PERMISSIONS_STORAGE, PermissionsUtils.REQUEST_STORAGE);
        } else {
            PermissionsUtils.requestPermissions(activity, PermissionsUtils.PERMISSIONS_STORAGE, PermissionsUtils.REQUEST_STORAGE);
        }
    }

    public void requestStoragePermissions(int requestCode) {
        if (PermissionsUtils.shouldShowRequestForStoragePermission(activity)) {
            showSnackBar(R.string.storage_permission, PermissionsUtils.PERMISSIONS_STORAGE, requestCode);
        } else {
            PermissionsUtils.requestPermissions(activity, PermissionsUtils.PERMISSIONS_STORAGE, requestCode);
        }
    }

    public void requestLocationPermissions() {
        if (PermissionsUtils.shouldShowRequestForLocationPermission(activity)) {
            showSnackBar(R.string.location_permission, PermissionsUtils.PERMISSIONS_LOCATION, PermissionsUtils.REQUEST_LOCATION);
        } else {
            PermissionsUtils.requestPermissions(activity, PermissionsUtils.PERMISSIONS_LOCATION, PermissionsUtils.REQUEST_LOCATION);
        }
    }

    public void requestAudio() {
        if (PermissionsUtils.shouldShowRequestForAudioPermission(activity)) {
            showSnackBar(R.string.record_audio, PermissionsUtils.PERMISSIONS_RECORD_AUDIO, PermissionsUtils.REQUEST_AUDIO_RECORD);
        } else {
            PermissionsUtils.requestPermissions(activity, PermissionsUtils.PERMISSIONS_RECORD_AUDIO, PermissionsUtils.REQUEST_AUDIO_RECORD);
        }
    }

    public void requestCameraPermission() {
        if (PermissionsUtils.shouldShowRequestForCameraPermission(activity)) {
            showSnackBar(R.string.phone_camera_permission, PermissionsUtils.PERMISSION_CAMERA, PermissionsUtils.REQUEST_CAMERA);
        } else {
            PermissionsUtils.requestPermissions(activity, PermissionsUtils.PERMISSION_CAMERA, PermissionsUtils.REQUEST_CAMERA);
        }
    }

    public void requestCameraPermission(int requestCode) {
        if (PermissionsUtils.shouldShowRequestForCameraPermission(activity)) {
            showSnackBar(R.string.phone_camera_permission, PermissionsUtils.PERMISSION_CAMERA, requestCode);
        } else {
            PermissionsUtils.requestPermissions(activity, PermissionsUtils.PERMISSION_CAMERA, requestCode);
        }
    }

    public void requestCameraPermissionForProfilePhoto() {
        if (PermissionsUtils.shouldShowRequestForCameraPermission(activity)) {
            showSnackBar(R.string.phone_camera_permission, PermissionsUtils.PERMISSION_CAMERA, PermissionsUtils.REQUEST_CAMERA_FOR_PROFILE_PHOTO);
        } else {
            PermissionsUtils.requestPermissions(activity, PermissionsUtils.PERMISSION_CAMERA, PermissionsUtils.REQUEST_CAMERA_FOR_PROFILE_PHOTO);
        }
    }

    public void requestStoragePermissionsForProfilePhoto() {
        if (PermissionsUtils.shouldShowRequestForStoragePermission(activity)) {
            showSnackBar(R.string.storage_permission, PermissionsUtils.PERMISSIONS_STORAGE, PermissionsUtils.REQUEST_STORAGE_FOR_PROFILE_PHOTO);
        } else {
            PermissionsUtils.requestPermissions(activity, PermissionsUtils.PERMISSIONS_STORAGE, PermissionsUtils.REQUEST_STORAGE_FOR_PROFILE_PHOTO);
        }
    }

    public void requestCameraAndRecordPermission() {
        if (PermissionsUtils.checkPermissionForCameraAndMicrophone(activity)) {
            showSnackBar(!PermissionsUtils.checkPermissionForCameraAndMicrophone(activity) ? R.string.camera_audio_permission : !PermissionsUtils.isAudioRecordingPermissionGranted(activity) ? R.string.record_audio : !PermissionsUtils.isCameraPermissionGranted(activity) ? R.string.phone_camera_permission : R.string.camera_audio_permission, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, PermissionsUtils.REQUEST_CAMERA_AUDIO);
        } else {
            PermissionsUtils.requestPermissions(activity, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, PermissionsUtils.REQUEST_CAMERA_AUDIO);
        }
    }

    public void checkRuntimePermissionForCameraAndAudioRecording() {
        if (PermissionsUtils.checkSelfPermissionForLocation(activity)) {
            requestCameraAndRecordPermission();
        }
    }

    public void checkRuntimePermissionForLocationActivity() {
        if (PermissionsUtils.checkSelfPermissionForLocation(activity)) {
            requestLocationPermissions();
        } else {
            ((MobicomLocationActivity) activity).processingLocation();
        }
    }

    public void showSnackBar(int resId, final String[] permissions, final int requestCode) {
        Snackbar.make(snackBarLayout, resId,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok_alert, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PermissionsUtils.requestPermissions(activity, permissions, requestCode);
                    }
                }).show();
    }

}
