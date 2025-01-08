package kommunicate.io.sample;

import android.content.Context;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import io.kommunicate.Kommunicate;
import io.kommunicate.app.BuildConfig;

/**
 * Created by ashish on 23/01/18.
 */

public class KommunicateApplication extends MultiDexApplication {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Kommunicate.changeAppLanguage(this, "hi");
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
