package io.kommunicate.ui.conversation.home;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import io.kommunicate.ui.R;

/**
 * Base of the home screen tabs that need the hosting activity.
 */
public abstract class KmHomeBaseFragment extends Fragment {

    @Nullable
    protected KmHomeHost host;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof KmHomeHost) {
            host = (KmHomeHost) context;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        applyTheme();
    }

    public void applyTheme() {
        View view = getView();
        if (view == null) {
            return;
        }
        view.setBackgroundColor(getBackgroundColor());
        applyTheme(view);
    }

    protected void applyTheme(@NonNull View view) {
    }

    protected boolean isDarkMode() {
        return host != null && host.isHomeDarkModeEnabled();
    }

    protected int getThemeColor(@ColorRes int light, @ColorRes int dark) {
        return ContextCompat.getColor(requireContext(), isDarkMode() ? dark : light);
    }

    protected int getBackgroundColor() {
        return getThemeColor(R.color.conversation_list_all_background, R.color.dark_mode_default);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        host = null;
    }
}
