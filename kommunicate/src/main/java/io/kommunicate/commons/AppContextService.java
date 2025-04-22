package io.kommunicate.commons;

import static io.kommunicate.utils.SentryUtils.configureSentryWithKommunicate;

import android.app.Application;
import android.content.Context;

import java.lang.ref.WeakReference;

public class AppContextService {
    private static Context application;

    public static Context getAppContext() {
        return application;
    }

    public static void initApp(Application application) {
        AppContextService.application = application;
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
            configureSentryWithKommunicate(context);
            if (context instanceof Application) {
                AppContextService.application = context;
            } else {
                AppContextService.application = context.getApplicationContext();
            }
        }
    }
}
