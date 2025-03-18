package io.kommunicate.commons.commons.core.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunil on 20/1/16.
 */
public class PermissionsUtils {

    public static final int REQUEST_STORAGE = 0;
    public static final int REQUEST_LOCATION = 1;
    public static final int REQUEST_PHONE_STATE = 2;
    public static final int REQUEST_AUDIO_RECORD = 3;
    public static final int REQUEST_CAMERA = 5;
    public static final int REQUEST_CAMERA_FOR_PROFILE_PHOTO = 7;
    public static final int REQUEST_STORAGE_FOR_PROFILE_PHOTO = 8;
    public static final int REQUEST_CAMERA_AUDIO = 9;
    public static String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    public static String[] PERMISSIONS_STORAGE = getStoragePermission();
    public static String[] PERMISSIONS_RECORD_AUDIO = {Manifest.permission.RECORD_AUDIO};
    public static String[] PERMISSION_CAMERA = {Manifest.permission.CAMERA};
    private static final String ANDROID_RECORD_AUDIO = "android.permission.RECORD_AUDIO";
    private static final String ANDROID_READ_MEDIA = "android.permission.READ_MEDIA";


    private static String[] getStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{ Manifest.permission.READ_MEDIA_AUDIO };
        } else {
            return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        }
    }

    public static String[] getStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
                List<String> permissions = new ArrayList<>();
                for (String permission : info.requestedPermissions){
                    if (permission.contains(ANDROID_READ_MEDIA)){
                        permissions.add(permission);
                    }
                }
                return permissions.toArray(new String[0]);
            } catch (Exception exception){
                exception.printStackTrace();
                return new String[]{ Manifest.permission.READ_MEDIA_AUDIO };
            }
        } else {
            return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        }
    }

    public static boolean verifyPermissions(int[] grantResults) {
        if (grantResults.length < 1) {
            return false;
        }

        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static boolean shouldShowRequestForLocationPermission(Activity activity) {
        return (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.ACCESS_FINE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.ACCESS_COARSE_LOCATION));
    }

    public static boolean shouldShowRequestForAudioPermission(Activity activity) {
        return (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.RECORD_AUDIO));
    }


    public static boolean shouldShowRequestForStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.READ_MEDIA_AUDIO));
        } else {
            return (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE));
        }
    }

    public static boolean shouldShowRequestForCameraPermission(Activity activity) {
        return (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.CAMERA));
    }

    public static boolean checkSelfForStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            boolean isGranted = false;

            for (String permission : getStoragePermission(activity.getApplicationContext())){
                isGranted = isGranted || (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED);
            }

            return isGranted;
        }
        else {
            return (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED);
        }
    }

    public static boolean checkSelfPermissionForLocation(Activity activity) {
        return (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED);
    }

    public static boolean checkSelfPermissionForAudioRecording(Activity activity) {
        return (ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED);
    }

    public static boolean checkSelfForCameraPermission(Activity activity) {
        return (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED);
    }

    public static void requestPermissions(Activity activity, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    public static boolean isAudioRecordingPermissionGranted(Context context) {
        String permission = ANDROID_RECORD_AUDIO;
        int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public static boolean isCameraPermissionGranted(Context context) {
        int res = context.checkCallingOrSelfPermission(Manifest.permission.CAMERA);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public static boolean checkPermissionForCameraAndMicrophone(Context context) {
        int resultCamera = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        int resultMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO);
        return resultCamera == PackageManager.PERMISSION_GRANTED &&
                resultMic == PackageManager.PERMISSION_GRANTED;
    }
}
