package com.applozic.mobicomkit.uiwidgets;

import android.app.Application;
import android.content.Context;

/**
 * Created by devashish on 28/4/14.
 */
public class ApplozicApplication extends Application {

    public static final String TITLE = "Chats";

    @Override
    public void onCreate() {
        // workaround for http://code.google.com/p/android/issues/detail?id=20915
        try {
            Class.forName("android.os.AsyncTask");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}
