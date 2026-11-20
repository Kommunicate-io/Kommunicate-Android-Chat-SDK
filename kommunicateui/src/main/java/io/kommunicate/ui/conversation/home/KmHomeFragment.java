package io.kommunicate.ui.conversation.home;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import io.kommunicate.ui.CustomizationSettings;
import io.kommunicate.ui.R;

/**
 * Welcome tab of the home screen. The header and button use the toolbar color and its title color,
 * so the screen follows the theme already configured for the app. Texts are string resources and
 * can be overridden by the host app.
 */
public class KmHomeFragment extends Fragment {

    public interface Host {
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

    private static final float CARD_RADIUS_DP = 28f;
    private static final float BUTTON_RADIUS_DP = 28f;
    private static final int DEFAULT_ACCENT_COLOR = Color.parseColor("#5C4BF5");

    private Host host;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Host) {
            host = (Host) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        host = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.km_fragment_home, container, false);
        int accentColor = host != null ? host.getHomeAccentColor() : DEFAULT_ACCENT_COLOR;
        int accentTextColor = host != null ? host.getHomeAccentTextColor() : Color.WHITE;
        float density = getResources().getDisplayMetrics().density;
        float cardRadius = CARD_RADIUS_DP * density;

        int[] textViewIds = {R.id.kmHomeGreetingEmoji, R.id.kmHomeGreeting, R.id.kmHomeWelcomeTitle, R.id.kmHomeViewConversations};
        for (int textViewId : textViewIds) {
            ((TextView) view.findViewById(textViewId)).setTextColor(accentTextColor);
        }

        GradientDrawable headerBackground = new GradientDrawable();
        headerBackground.setColor(accentColor);
        headerBackground.setCornerRadii(new float[]{cardRadius, cardRadius, cardRadius, cardRadius, 0, 0, 0, 0});
        view.findViewById(R.id.kmHomeWelcomeHeader).setBackground(headerBackground);

        GradientDrawable cardBackground = new GradientDrawable();
        cardBackground.setColor(Color.WHITE);
        cardBackground.setCornerRadius(cardRadius);
        view.findViewById(R.id.kmHomeWelcomeCard).setBackground(cardBackground);

        GradientDrawable buttonBackground = new GradientDrawable();
        buttonBackground.setColor(accentColor);
        buttonBackground.setCornerRadius(BUTTON_RADIUS_DP * density);
        View button = view.findViewById(R.id.kmHomeViewConversations);
        button.setBackground(buttonBackground);
        button.setOnClickListener(v -> {
            if (host != null) {
                host.onViewConversationsClicked();
            }
        });
        return view;
    }
}
