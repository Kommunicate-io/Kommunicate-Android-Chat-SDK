package com.applozic.mobicommons;

import android.app.Application;
import android.content.Context;

import java.lang.ref.WeakReference;

public class ApplozicService {
    private static Context application;

    public static Context getAppContext() {
        return application;
    }

    public static void initApp(Application application) {
        ApplozicService.application = application;
    }

    public static Context getContext(Context context) {
        if (application == null && context != null) {
            application = context instanceof Application ? context : context.getApplicationContext();
        }
        return application;
    }

    public static Context getContextFromWeak(WeakReference<Context> contextWeakReference) {
        if (application == null && contextWeakReference != null) {
            application = contextWeakReference.get() instanceof Application ? contextWeakReference.get() : contextWeakReference.get().getApplicationContext();
        }
        return application;
    }

    public static void initWithContext(Context context) {
        if (context != null && application == null) {
            if (context instanceof Application) {
                ApplozicService.application = context;
            } else {
                ApplozicService.application = context.getApplicationContext();
            }
        }
    }
}
