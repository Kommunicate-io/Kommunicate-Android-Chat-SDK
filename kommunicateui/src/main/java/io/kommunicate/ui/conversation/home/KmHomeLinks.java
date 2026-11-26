package io.kommunicate.ui.conversation.home;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;

/**
 * Opens the links of the home screen tabs. Only http, https and mailto links are opened.
 */
final class KmHomeLinks {

    private KmHomeLinks() {
    }

    static boolean isOpenable(@Nullable String link) {
        if (TextUtils.isEmpty(link)) {
            return false;
        }
        String scheme = Uri.parse(link).getScheme();
        return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme) || "mailto".equalsIgnoreCase(scheme);
    }

    static void open(@NonNull Context context, @NonNull Uri uri) {
        if (!isOpenable(uri.toString())) {
            return;
        }
        // Intent filters match a lower case scheme only.
        Uri target = uri.normalizeScheme();
        try {
            if ("mailto".equals(target.getScheme())) {
                context.startActivity(new Intent(Intent.ACTION_SENDTO, target));
            } else {
                new CustomTabsIntent.Builder().build().launchUrl(context, target);
            }
        } catch (ActivityNotFoundException noHandler) {
            // Nothing can open this link on the device.
        }
    }
}
