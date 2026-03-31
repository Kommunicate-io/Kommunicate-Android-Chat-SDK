package io.kommunicate.ui.activities;

import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import io.kommunicate.ui.CustomizationSettings;

public abstract class KmBaseActivity extends AppCompatActivity {

    protected void setupEdgeToEdge(boolean lightStatusBar, int statusBarColor) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(lightStatusBar);
            controller.setAppearanceLightNavigationBars(lightStatusBar);
        }
    }

    protected void setupEdgeToEdge(CustomizationSettings customizationSettings, boolean lightStatusBar, int statusBarColor) {
        if (customizationSettings == null || !customizationSettings.isEnableEdgeToEdge()) {
            return;
        }
        setupEdgeToEdge(lightStatusBar, statusBarColor);
    }

    protected void setupEdgeToEdge(CustomizationSettings customizationSettings, boolean lightStatusBar) {
        setupEdgeToEdge(customizationSettings, lightStatusBar, 0);
    }

    protected void setupEdgeToEdge(CustomizationSettings customizationSettings) {
        setupEdgeToEdge(customizationSettings, shouldUseLightSystemBars(customizationSettings));
    }

    protected boolean shouldUseLightSystemBars(CustomizationSettings customizationSettings) {
        boolean darkModeEnabled = customizationSettings != null
                && customizationSettings.getUseDarkMode()
                && ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES);
        return !darkModeEnabled;
    }

    protected void setupEdgeToEdge(boolean lightStatusBar) {
        setupEdgeToEdge(lightStatusBar, 0);
    }

    protected void setupEdgeToEdge() {
        setupEdgeToEdge(true);
    }
}
