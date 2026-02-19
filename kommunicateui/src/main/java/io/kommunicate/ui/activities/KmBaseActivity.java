package io.kommunicate.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public abstract class KmBaseActivity extends AppCompatActivity {

    protected void setupEdgeToEdge(boolean lightStatusBar, int statusBarColor) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(statusBarColor);
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(lightStatusBar);
            controller.setAppearanceLightNavigationBars(lightStatusBar);
        }
    }

    protected void setupEdgeToEdge(boolean lightStatusBar) {
        setupEdgeToEdge(lightStatusBar, 0);
    }

    protected void setupEdgeToEdge() {
        setupEdgeToEdge(true);
    }
}
