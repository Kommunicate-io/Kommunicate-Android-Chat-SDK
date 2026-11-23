package io.kommunicate.ui.conversation.home;

import io.kommunicate.ui.CustomizationSettings;

/**
 * What the home screen tabs need from the activity that hosts them.
 */
public interface KmHomeHost {
    /**
     * @return true when the bottom navigation home screen is used instead of the plain conversation list.
     */
    boolean isHomeScreenActive();

    /**
     * Background color of the header, the button and the selected tab (the toolbar color).
     */
    int getHomeAccentColor();

    /**
     * Color of texts drawn on top of {@link #getHomeAccentColor()} (the toolbar title color).
     */
    int getHomeAccentTextColor();

    CustomizationSettings getHomeCustomizationSettings();

    void onViewConversationsClicked();
}
