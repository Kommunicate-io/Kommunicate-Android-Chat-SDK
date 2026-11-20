package io.kommunicate.ui.conversation.home;

import android.content.Context;
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
 * Bottom navigation of the home screen. The tab list is supplied by the caller so the bar can be
 * driven by settings; the accent color follows the theme primary color.
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
    private final OnTabSelectedListener listener;
    private final List<KmHomeTab> tabs = new ArrayList<>();
    private final List<View> tabViews = new ArrayList<>();
    private KmHomeTab selectedTab;

    public KmHomeBottomNav(ViewGroup container, int accentColor, OnTabSelectedListener listener) {
        this.container = container;
        this.accentColor = accentColor;
        this.listener = listener;
    }

    public void setTabs(List<KmHomeTab> newTabs) {
        if (tabs.equals(newTabs)) {
            return;
        }
        tabs.clear();
        tabs.addAll(newTabs);
        tabViews.clear();
        container.removeAllViews();

        Context context = container.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        for (final KmHomeTab tab : tabs) {
            View tabView = inflater.inflate(R.layout.km_home_tab_item, container, false);
            ((TextView) tabView.findViewById(R.id.kmHomeTabLabel)).setText(tab.getLabelRes());
            ((ImageView) tabView.findViewById(R.id.kmHomeTabIcon)).setImageResource(tab.getIconRes());
            tabView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (tab != selectedTab && listener != null) {
                        listener.onTabSelected(tab);
                    }
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
                    android.content.res.ColorStateList.valueOf(selected ? Color.WHITE : INACTIVE_ICON_COLOR));
            label.setTextColor(selected ? ACTIVE_LABEL_COLOR : INACTIVE_LABEL_COLOR);
            tabView.setSelected(selected);
        }
    }
}
