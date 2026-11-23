package io.kommunicate.ui.conversation.home;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
    public void onDetach() {
        super.onDetach();
        host = null;
    }
}
