package io.kommunicate.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import androidx.core.content.ContextCompat;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicommons.commons.core.utils.Utils;

public class KmUtils {

    private static final String TAG = "Kommunicate";

    public static boolean isServiceDisconnected(Context context, boolean isAgentApp, RelativeLayout customToolbarLayout) {
        boolean isDebuggable = (0 != (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
        boolean disconnect = !isAgentApp
                && MobiComUserPreference.getInstance(context).getPricingPackage() == PackageType.STARTUP.getValue()
                && !isDebuggable;
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

    public static void setDrawableTint(TextView textView, int colorId, int index) {
        textView.getCompoundDrawables()[index]
                .setColorFilter(new PorterDuffColorFilter(colorId, PorterDuff.Mode.SRC_IN));
    }

    public static void showToastAndLog(Context context, int messageResId) {
        Toast.makeText(context, messageResId, Toast.LENGTH_LONG).show();
        Utils.printLog(context, TAG, Utils.getString(context, messageResId));
    }

    public static boolean isAgent(Context context) {
        return User.RoleType.AGENT.getValue().equals(MobiComUserPreference.getInstance(context).getUserRoleType());
    }

    public enum PackageType {
        STARTUP(101),
        PER_AGENT_MONTHLY(102),
        PER_AGENT_YEARLY(103),
        GROWTH_MONTHLY(104),
        ENTERPRISE_MONTHLY(105),
        ENTERPRISE_YEARLY(106),
        EARLY_BIRD_MONTHLY(107),
        EARLY_BIRD_YEARLY(108);

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
            throw new ClassNotFoundException("No class found for name : " + className);
        }
    }
}
