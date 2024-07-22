package com.applozic.mobicommons.commons.core.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.os.StrictMode;
import android.provider.Settings;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.applozic.mobicommons.ALSpecificSettings;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.file.ALFileProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;


/**
 * This class contains static utility methods.
 */
public class Utils {

    private static final String TAG = "Utils";

    // Prevents instantiation.
    private Utils() {
    }

    /**
     * Enables strict mode. This should only be called when debugging the application and is useful
     * for finding some potential bugs or best practice violations.
     */
    @TargetApi(11)
    public static void enableStrictMode() {
        // Strict mode is only available on gingerbread or later
        if (Utils.hasGingerbread()) {

            // Enable all thread strict mode policies
            StrictMode.ThreadPolicy.Builder threadPolicyBuilder =
                    new StrictMode.ThreadPolicy.Builder()
                            .detectAll()
                            .penaltyLog();

            // Enable all VM strict mode policies
            StrictMode.VmPolicy.Builder vmPolicyBuilder =
                    new StrictMode.VmPolicy.Builder()
                            .detectAll()
                            .penaltyLog();

            // Honeycomb introduced some additional strict mode features
            if (Utils.hasHoneycomb()) {
                // Flash screen when thread policy is violated
                threadPolicyBuilder.penaltyFlashScreen();
                // For each activity class, set an instance limit of 1. Any more instances and
                // there could be a memory leak.
               /* vmPolicyBuilder
                        .setClassInstanceLimit(ContactActivity.class, 1);*/
            }

            // Use builders to enable strict mode policies
            StrictMode.setThreadPolicy(threadPolicyBuilder.build());
            StrictMode.setVmPolicy(vmPolicyBuilder.build());
        }
    }

    /**
     * Uses static final constants to detect if the device's platform version is Froyo or
     * later.
     */
    public static boolean hasFroyo() {

        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    /**
     * Uses static final constants to detect if the device's platform version is Gingerbread or
     * later.
     */
    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    /**
     * Uses static final constants to detect if the device's platform version is Honeycomb or
     * later.
     */
    public static boolean hasHoneycomb() {

        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;


    }

    /**
     * Uses static final constants to detect if the device's platform version is Honeycomb MR1 or
     * later.
     */
    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    /**
     * Uses static final constants to detect if the device's platform version is JellyBean MR1 or
     * later.
     */
    public static boolean hasJellyBeanMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    /**
     * Uses static final constants to detect if the device's platform version is ICS or
     * later.
     */

    public static boolean hasICS() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    public static boolean hasKitkat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean hasMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean hasNougat() {
        return Build.VERSION.SDK_INT >= 24;
    }


    public static boolean isBetweenGingerBreadAndKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT;
    }

    public static boolean isDeviceInIdleState(Context context) {
        if (hasMarshmallow()) {
            PowerManager pm = context.getSystemService(PowerManager.class);
            if (pm != null && pm.isDeviceIdleMode()) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasOreo() {
        return Build.VERSION.SDK_INT >= 26;
    }

    public static void toggleSoftKeyBoard(Activity activity, boolean hide) {
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            return;
        }
        if (hide) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } else {
            inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) ApplozicService.getContext(context).getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting());

    }

    public static String getMetaDataValue(Context context, String metaDataName) {
        try {
            PackageManager packageManager = ApplozicService.getContext(context).getPackageManager();
            ApplicationInfo ai = packageManager.getApplicationInfo(ApplozicService.getContext(context).getPackageName(), PackageManager.GET_META_DATA);
            if (ai.metaData != null) {
                return ai.metaData.getString(metaDataName);

            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public static boolean isAutomaticTimeEnabled(Context context, boolean isTimeZone) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.Global.getInt(ApplozicService.getContext(context).getContentResolver(), (isTimeZone ? Settings.Global.AUTO_TIME_ZONE : Settings.Global.AUTO_TIME), 0) == 1;
        } else {
            return android.provider.Settings.System.getInt(ApplozicService.getContext(context).getContentResolver(), (isTimeZone ? Settings.System.AUTO_TIME_ZONE : Settings.System.AUTO_TIME), 0) == 1;
        }
    }

    public static int getLauncherIcon(Context context) {
        try {
            ApplicationInfo ai = ApplozicService.getContext(context).getPackageManager().getApplicationInfo(ApplozicService.getContext(context).getPackageName(), PackageManager.GET_META_DATA);
            return ai.icon;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static Integer getMetaDataValueForResources(Context context, String metaDataName) {
        try {
            ApplicationInfo ai = ApplozicService.getContext(context).getPackageManager().getApplicationInfo(ApplozicService.getContext(context).getPackageName(), PackageManager.GET_META_DATA);
            if (ai.metaData != null) {
                Integer metaDataValue = ai.metaData.getInt(metaDataName);
                return metaDataValue == 0 ? null : metaDataValue;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }


    public static String getMetaDataValueForReceiver(Context context, String componentName, String metaDataName) {
        try {
            ActivityInfo ai = ApplozicService.getContext(context).getPackageManager().getReceiverInfo(new ComponentName(ApplozicService.getContext(context), componentName), PackageManager.GET_META_DATA);
            return ai.metaData.getString(metaDataName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static String makePlaceHolders(int len) {
        if (len < 1) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder(len * 2 - 1);
            sb.append("?");
            for (int i = 1; i < len; i++) {
                sb.append(",?");
            }
            return sb.toString();
        }
    }


    public static CharSequence getStyleString(String name) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(name);
        return builder;
    }

    public static CharSequence getStyledStringForContact(String displayName, String message) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(displayName).append(": ").append(message);
        return builder;
    }

    public static CharSequence getStyledStringForChannel(String name, String channelName, String message) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(name).append(" @ ").append(channelName).append(": ").append(message);
        return builder;
    }

    public static CharSequence getStyleStringForMessage(String message) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(message);
        return builder;
    }

    public static String getTimeDurationInFormat(Long timeInMillis) {

        long timeInSecond = timeInMillis / 1000;
        String formattedDuration;

        if (timeInSecond < 60) {
            return (timeInSecond + " Sec");
        } else {
            formattedDuration = timeInSecond / 60 + " Min";
            if (timeInSecond % 60 > 0) {
                formattedDuration = formattedDuration + " " + timeInSecond % 60 + " Sec";
            }
        }

        return formattedDuration;
    }

    public static void printLog(Context context, String tag, String message) {
        try {
            if (isDebugBuild(context) || ALSpecificSettings.getInstance(context).isLoggingEnabledForReleaseBuild()) {
                Log.i(tag, message);

                if (ALSpecificSettings.getInstance(context).isTextLoggingEnabled()) {
                    writeToFile(context, tag + " (" + DateUtils.getDateAndTimeInDefaultFormat(System.currentTimeMillis()) + ") : " + message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isDebugBuild(Context context) {
        return (0 != (ApplozicService.getContext(context).getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
    }

    public static void writeToFile(Context context, String log) {
        try {
            String fileName = "/" + ALSpecificSettings.getInstance(context).getTextLogFileName() + ".txt";
            BufferedWriter bufferedWriter = null;
            try {
                String folder = "/" + Utils.getMetaDataValue(context, "main_folder_name");
                File dir = new File(context.getFilesDir().getAbsolutePath() + folder);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(dir, fileName);
                if (!file.exists()) {
                    file.createNewFile();
                }

                FileWriter writer = new FileWriter(file, true);
                bufferedWriter = new BufferedWriter(writer);
                bufferedWriter.append(log);
                bufferedWriter.append("\r\n\n");

                bufferedWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Uri getTextLogFileUri(Context context) {
        try {
            String fileName = "/" + ALSpecificSettings.getInstance(context).getTextLogFileName() + ".txt";
            String folder = "/" + Utils.getMetaDataValue(context, "main_folder_name");
            File dir = new File(context.getFilesDir().getAbsolutePath() + folder);
            File textLogFile = new File(dir, fileName);
            if (hasNougat()) {
                return ALFileProvider.getUriForFile(ApplozicService.getContext(context), getMetaDataValue(context, "com.package.name") + ".applozic.provider", textLogFile);
            }
            return Uri.fromFile(textLogFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getString(Context context, int resId) {
        return ApplozicService.getContext(context).getString(resId);
    }

    public static int getColor(Context context, int resId) {
        return ApplozicService.getContext(context).getResources().getColor(resId);
    }

    public static String getPackageName(Context context) {
        return ApplozicService.getContext(context).getPackageName();
    }

    public static HashMap<String, String> getUploadOverrideMap(String string) {
        try {
            JSONObject jsonObject = new JSONObject(string);
            JSONObject jsonObjec2 = new JSONObject(jsonObject.getString("metadata"));
            HashMap<String, String> newMetadata = new HashMap<>();
            newMetadata.put("payload", jsonObjec2.getString("payload"));
            newMetadata.put("templateId", jsonObjec2.getString("templateId"));
            newMetadata.put("contentType", "300");
            return newMetadata;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
