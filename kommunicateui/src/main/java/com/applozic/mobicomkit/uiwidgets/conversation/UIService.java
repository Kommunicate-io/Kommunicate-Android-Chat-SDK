package com.applozic.mobicomkit.uiwidgets.conversation;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

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
        if (activity == null) {
            return null;
        }
        FragmentManager supportFragmentManager = activity.getSupportFragmentManager();

        if (supportFragmentManager.getBackStackEntryCount() == 0) {
            return null;
        }
        return supportFragmentManager.findFragmentByTag(tag);
    }

}