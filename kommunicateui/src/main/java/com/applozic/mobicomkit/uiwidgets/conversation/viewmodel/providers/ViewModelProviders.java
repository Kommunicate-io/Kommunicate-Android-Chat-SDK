package com.applozic.mobicomkit.uiwidgets.conversation.viewmodel.providers;

import android.annotation.SuppressLint;
import android.app.Application;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProvider.Factory;

import java.lang.reflect.InvocationTargetException;

public class ViewModelProviders {
    @SuppressLint("StaticFieldLeak")
    private static DefaultFactory sDefaultFactory;

    private static void initializeFactoryIfNeeded(Application application) {
        if (sDefaultFactory == null) {
            sDefaultFactory = new DefaultFactory(application);
        }
    }

    /**
     * Creates a {@link ViewModelProvider}, which retains ViewModels while a scope of given
     * {@code fragment} is alive. More detailed explanation is in {@link ViewModel}.
     * <p>
     * It uses {@link DefaultFactory} to instantiate new ViewModels.
     *
     * @param fragment a fragment, in whose scope ViewModels should be retained
     * @return a ViewModelProvider instance
     */
    @MainThread
    public static ViewModelProvider of(@NonNull Fragment fragment) {
        FragmentActivity activity = fragment.getActivity();
        if (activity == null) {
            throw new IllegalArgumentException(
                    "Can't create ViewModelProvider for detached fragment");
        }
        initializeFactoryIfNeeded(activity.getApplication());
        return new ViewModelProvider(ViewModelStores.of(fragment), sDefaultFactory);
    }

    /**
     * Creates a {@link ViewModelProvider}, which retains ViewModels while a scope of given Activity
     * is alive. More detailed explanation is in {@link ViewModel}.
     * <p>
     * It uses {@link DefaultFactory} to instantiate new ViewModels.
     *
     * @param activity an activity, in whose scope ViewModels should be retained
     * @return a ViewModelProvider instance
     */
    @MainThread
    public static ViewModelProvider of(@NonNull FragmentActivity activity) {
        initializeFactoryIfNeeded(activity.getApplication());
        return new ViewModelProvider(ViewModelStores.of(activity), sDefaultFactory);
    }

    /**
     * Creates a {@link ViewModelProvider}, which retains ViewModels while a scope of given
     * {@code fragment} is alive. More detailed explanation is in {@link ViewModel}.
     * <p>
     * It uses the given {@link Factory} to instantiate new ViewModels.
     *
     * @param fragment a fragment, in whose scope ViewModels should be retained
     * @param factory  a {@code Factory} to instantiate new ViewModels
     * @return a ViewModelProvider instance
     */
    @MainThread
    public static ViewModelProvider of(@NonNull Fragment fragment, @NonNull Factory factory) {
        return new ViewModelProvider(ViewModelStores.of(fragment), factory);
    }

    /**
     * Creates a {@link ViewModelProvider}, which retains ViewModels while a scope of given Activity
     * is alive. More detailed explanation is in {@link ViewModel}.
     * <p>
     * It uses the given {@link Factory} to instantiate new ViewModels.
     *
     * @param activity an activity, in whose scope ViewModels should be retained
     * @param factory  a {@code Factory} to instantiate new ViewModels
     * @return a ViewModelProvider instance
     */
    @MainThread
    public static ViewModelProvider of(@NonNull FragmentActivity activity,
                                       @NonNull Factory factory) {
        return new ViewModelProvider(ViewModelStores.of(activity), factory);
    }

    /**
     * {@link Factory} which may create {@link AndroidViewModel} and
     * {@link ViewModel}, which have an empty constructor.
     */
    @SuppressWarnings("WeakerAccess")
    public static class DefaultFactory extends ViewModelProvider.NewInstanceFactory {

        private Application mApplication;

        /**
         * Creates a {@code DefaultFactory}
         *
         * @param application an application to pass in {@link AndroidViewModel}
         */
        public DefaultFactory(@NonNull Application application) {
            mApplication = application;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (AndroidViewModel.class.isAssignableFrom(modelClass)) {
                //noinspection TryWithIdenticalCatches
                try {
                    return modelClass.getConstructor(Application.class).newInstance(mApplication);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException("Cannot create an instance of " + modelClass, e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cannot create an instance of " + modelClass, e);
                } catch (InstantiationException e) {
                    throw new RuntimeException("Cannot create an instance of " + modelClass, e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException("Cannot create an instance of " + modelClass, e);
                }
            }
            return super.create(modelClass);
        }
    }
}
