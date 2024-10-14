package io.kommunicate.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;

import androidx.core.content.ContextCompat;

import io.kommunicate.KmSettings;
import io.kommunicate.nativeLibs.FridaDetection;
import io.kommunicate.nativeLibs.RootDetection;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.api.attachment.FileMeta;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.file.FileUtils;
import com.applozic.mobicommons.json.GsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static io.kommunicate.Kommunicate.KM_CHAT_CONTEXT;

public class KmUtils {

    private static final String TAG = "Kommunicate";
    public static final int LEFT_POSITION = 0;
    public static final int RIGHT_POSITION = 2;
    public static final String BOT_CUSTOMIZATION = "bot_customization";
    public static final String NAME = "name";
    public static final String ID = "id";
    private static final String NO_CLASS_FOUND = "No class found for name : ";


    public static boolean isServiceDisconnected(Context context, boolean isAgentApp, RelativeLayout customToolbarLayout) {
        boolean isDebuggable = (0 != (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
        boolean disconnect = (!isAgentApp && !isDebuggable) && (MobiComUserPreference.getInstance(context).getPricingPackage() == PackageType.STARTUP.getValue()
                || MobiComUserPreference.getInstance(context).getPricingPackage() == PackageType.START_MONTHLY.getValue()
                || MobiComUserPreference.getInstance(context).getPricingPackage() == PackageType.START_YEARLY.getValue());
        if (customToolbarLayout != null) {
            customToolbarLayout.setVisibility(View.GONE);
        }
        return disconnect;
    }

    public static void setBackground(Context context, View view, int resId) {
        final int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackgroundDrawable(ContextCompat.getDrawable(context, resId));
        } else {
            view.setBackground(ContextCompat.getDrawable(context, resId));
        }
    }

    public static boolean isDeviceRooted() {
        KmAppSettingPreferences preferences = KmAppSettingPreferences.getInstance();
        if (!preferences.isRootDetectionEnabled()) {
            return false;
        }

        // Native Frida process detection
        if (FridaDetection.isFridaDetectedNative()) {
            return true;
        }

        // Native Frida memory library detection
        if (FridaDetection.detectFridaByLibrary()) {
            return true;
        }

        // Detect hooking of key functions
        if (FridaDetection.checkForHooking()) {
            return true;
        }

        return RootDetection.isDeviceRooted();
    }

    public static void setDrawableTint(TextView textView, int colorId, int index) {
        textView.getCompoundDrawables()[index]
                .setColorFilter(new PorterDuffColorFilter(colorId, PorterDuff.Mode.SRC_IN));
    }

    public static void setDrawableTint(ImageView imageView, int colorId) {
        imageView.getDrawable()
                .setColorFilter(new PorterDuffColorFilter(colorId, PorterDuff.Mode.SRC_IN));
    }

    public static void showToastAndLog(Context context, int messageResId) {
        Toast.makeText(context, messageResId, Toast.LENGTH_LONG).show();
        Utils.printLog(context, TAG, Utils.getString(context, messageResId));
    }

    public static boolean isAgent(Context context) {
        return User.RoleType.AGENT.getValue().equals(MobiComUserPreference.getInstance(context).getUserRoleType());
    }

    public static boolean isAgent() {
        return isAgent(ApplozicService.getAppContext());
    }

    public static void setGradientSolidColor(View view, int color) {
        GradientDrawable gradientDrawable = (GradientDrawable) view.getBackground();
        gradientDrawable.setColor(color);
    }

    public static void setGradientStrokeColor(View view, int width, int color) {
        GradientDrawable gradientDrawable = (GradientDrawable) view.getBackground();
        gradientDrawable.setStroke(width, color);
    }

    public static void setIconInsideTextView(TextView textView, int drawableRes, int color, int position, int padding, boolean isDarKMode) {
        if (position == LEFT_POSITION) {
            textView.setCompoundDrawablesWithIntrinsicBounds(drawableRes, 0, 0, 0);
        } else if (position == RIGHT_POSITION) {
            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableRes, 0);
        }
        textView.setCompoundDrawablePadding(padding);
        if (color != Color.TRANSPARENT) {
            textView.getCompoundDrawables()[position].setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        }
        if (isDarKMode) {
            textView.getCompoundDrawables()[position].setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN));
        }
    }

    public static void setIconInsideTextView(TextView textView) {
        textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
    }

    public static void setStatusBarColor(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }

    public static Drawable getDrawable(Context context, int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return context.getDrawable(resId);
        }
        return ContextCompat.getDrawable(context, resId);
    }

    public static String getCustomBotName(Message message, Context context) {
        if (message != null) {
            Map<String, String> metadata = new HashMap<>();
            metadata = (Map<String, String>) GsonUtils.getObjectFromJson(ApplozicClient.getInstance(context).getMessageMetaData(), Map.class);
            if (metadata != null && metadata.containsKey(KmSettings.KM_CHAT_CONTEXT) && metadata.get(KM_CHAT_CONTEXT).contains(BOT_CUSTOMIZATION)) {
                JSONObject custombotObject = null;
                try {
                    custombotObject = new JSONObject(metadata.get(KM_CHAT_CONTEXT));
                    JSONObject botDataObject = new JSONObject(custombotObject.getString(BOT_CUSTOMIZATION));
                    if (!TextUtils.isEmpty(message.getContactIds()) && botDataObject.has(ID) && message.getContactIds().equals(botDataObject.getString(ID))) {
                        return botDataObject.getString(NAME);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static String getAttachmentName(Message message) {
        if (message == null) {
            return "File";
        }
        FileMeta fileMeta = message.getFileMetas();
        if (fileMeta == null && message.getFilePaths() != null) {
            return new File(message.getFilePaths().get(0)).getName().replace(KmConstants.AWS_ENCRYPTED, "").replace(String.valueOf(message.getCreatedAtTime()), "");
        } else if (message.getFilePaths() != null) {
            return new File(message.getFilePaths().get(0)).getName().replace(KmConstants.AWS_ENCRYPTED, "").replace(String.valueOf(message.getCreatedAtTime()), "");
        }
        if (fileMeta != null && fileMeta.getName() != null) {
            String fileName = FileUtils.getName(fileMeta.getName()) + message.getCreatedAtTime() + "." + FileUtils.getFileFormat(fileMeta.getName());
            return fileName.replace(KmConstants.AWS_ENCRYPTED, "").replace(String.valueOf(message.getCreatedAtTime()), "");
        }
        return "File";
    }

    public enum PackageType {
        STARTUP(101),
        PER_AGENT_MONTHLY(102),
        PER_AGENT_YEARLY(103),
        GROWTH_MONTHLY(104),
        ENTERPRISE_MONTHLY(105),
        ENTERPRISE_YEARLY(106),
        EARLY_BIRD_MONTHLY(107),
        EARLY_BIRD_YEARLY(108),
        START_MONTHLY(112),
        START_YEARLY(113);

        private int value;

        PackageType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static Class getClassFromName(String className) throws ClassNotFoundException {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundException(NO_CLASS_FOUND + className);
        }
    }
}
