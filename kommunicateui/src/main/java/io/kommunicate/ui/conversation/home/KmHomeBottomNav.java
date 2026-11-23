package io.kommunicate.ui.conversation.home;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.widget.ImageViewCompat;

import java.util.ArrayList;
import java.util.List;

import io.kommunicate.ui.R;

/**
 * Bottom navigation of the home screen. The caller supplies the tabs and the accent colors.
 */
public class KmHomeBottomNav {

    public interface OnTabSelectedListener {
        void onTabSelected(KmHomeTab tab);
    }

    private static final int INACTIVE_ICON_COLOR = Color.parseColor("#2B2B38");
    private static final int INACTIVE_BACKGROUND_COLOR = Color.parseColor("#F1F1F4");
    private static final int ACTIVE_LABEL_COLOR = Color.parseColor("#1B1B3A");
    private static final int INACTIVE_LABEL_COLOR = Color.parseColor("#3C3C48");

    private final ViewGroup container;
    private final int accentColor;
    private final int accentIconColor;
    private final OnTabSelectedListener listener;
    private final List<KmHomeTab> tabs = new ArrayList<>();
    private final List<View> tabViews = new ArrayList<>();
    private KmHomeTab selectedTab;

    /**
     * @param accentColor     background of the selected tab icon
     * @param accentIconColor color of the selected tab icon, drawn on {@code accentColor}
     */
    public KmHomeBottomNav(ViewGroup container, int accentColor, int accentIconColor, OnTabSelectedListener listener) {
        this.container = container;
        this.accentColor = accentColor;
        this.accentIconColor = accentIconColor;
        this.listener = listener;
    }

    public void setTabs(List<KmHomeTab> newTabs) {
        tabs.clear();
        tabs.addAll(newTabs);
        tabViews.clear();
        container.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        for (KmHomeTab tab : tabs) {
            View tabView = inflater.inflate(R.layout.km_home_tab_item, container, false);
            ((TextView) tabView.findViewById(R.id.kmHomeTabLabel)).setText(tab.getLabelRes());
            ((ImageView) tabView.findViewById(R.id.kmHomeTabIcon)).setImageResource(tab.getIconRes());
            tabView.setOnClickListener(v -> {
                if (tab != selectedTab) {
                    listener.onTabSelected(tab);
                }
            });
            container.addView(tabView);
            tabViews.add(tabView);
        }
        applySelection();
    }

    public void select(KmHomeTab tab) {
        selectedTab = tab;
        applySelection();
    }

    public void setVisible(boolean visible) {
        container.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void applySelection() {
        for (int i = 0; i < tabViews.size(); i++) {
            boolean selected = tabs.get(i) == selectedTab;
            View tabView = tabViews.get(i);
            FrameLayout iconContainer = tabView.findViewById(R.id.kmHomeTabIconContainer);
            ImageView icon = tabView.findViewById(R.id.kmHomeTabIcon);
            TextView label = tabView.findViewById(R.id.kmHomeTabLabel);

            GradientDrawable background = (GradientDrawable) iconContainer.getBackground().mutate();
            background.setColor(selected ? accentColor : INACTIVE_BACKGROUND_COLOR);
            ImageViewCompat.setImageTintList(icon,
                    ColorStateList.valueOf(selected ? accentIconColor : INACTIVE_ICON_COLOR));
            label.setTextColor(selected ? ACTIVE_LABEL_COLOR : INACTIVE_LABEL_COLOR);
        }
    }
}
