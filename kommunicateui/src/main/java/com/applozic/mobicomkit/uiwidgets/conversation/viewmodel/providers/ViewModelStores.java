package com.applozic.mobicomkit.uiwidgets.conversation.viewmodel.providers;

import androidx.annotation.MainThread;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelStore;

import static com.applozic.mobicomkit.uiwidgets.conversation.viewmodel.providers.HolderFragment.holderFragmentFor;

@SuppressWarnings("WeakerAccess")
public class ViewModelStores {
    private ViewModelStores() {
    }

    /**
     * Returns the {@link ViewModelStore} of the given activity.
     *
     * @param activity an activity whose {@code ViewModelStore} is requested
     * @return a {@code ViewModelStore}
     */
    @MainThread
    public static ViewModelStore of(FragmentActivity activity) {
        return holderFragmentFor(activity).getViewModelStore();
    }

    /**
     * Returns the {@link ViewModelStore} of the given fragment.
     *
     * @param fragment a fragment whose {@code ViewModelStore} is requested
     * @return a {@code ViewModelStore}
     */
    @MainThread
    public static ViewModelStore of(Fragment fragment) {
        return holderFragmentFor(fragment).getViewModelStore();
    }
}
