package com.applozic.mobicomkit.uiwidgets.conversation;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

/**
 * Created by devashish on 6/27/2015.
 */
public class UIService {

    /*public static Fragment getActiveFragment(FragmentActivity activity) {
        FragmentManager supportFragmentManager = activity.getSupportFragmentManager();


        if (supportFragmentManager.getBackStackEntryCount() == 0) {
            return null;
        }
        String tag = supportFragmentManager.getBackStackEntryAt(
                supportFragmentManager.getBackStackEntryCount() - 1)
                .getName();
        return supportFragmentManager
                .findFragmentByTag(tag);
    }*/

    public static Fragment getFragmentByTag(FragmentActivity activity, String tag) {
        FragmentManager supportFragmentManager = activity.getSupportFragmentManager();

        if (supportFragmentManager.getBackStackEntryCount() == 0) {
            return null;
        }
        return supportFragmentManager.findFragmentByTag(tag);
    }

}