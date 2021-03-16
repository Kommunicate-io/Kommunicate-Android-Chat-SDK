package androidx.core.app;

import android.os.Build;

public abstract class AlJobIntentService extends JobIntentService {

    @Override
    GenericWorkItem dequeueWork() {
        try {
            return super.dequeueWork();
        } catch (SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // override mJobImpl with safe class to ignore SecurityException
        if (Build.VERSION.SDK_INT >= 26) {
            mJobImpl = new androidx.core.app.AlJobServiceEngineImpl(this);
        } else {
            mJobImpl = null;
        }
    }
}
