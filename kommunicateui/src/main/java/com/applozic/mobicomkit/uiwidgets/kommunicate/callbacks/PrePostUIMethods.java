package com.applozic.mobicomkit.uiwidgets.kommunicate.callbacks;

import java.io.File;

/**
 * the callback to the pre and post methods to doInBackground for a async task
 * can be used to implement the pre post functionality according to our needs
 * currently, will be used for the FileTaskAsync
 *
 * @author shubham
 * 22 September, 2019
 */
public interface PrePostUIMethods {
    void preTaskUIMethod();

    void postTaskUIMethod(boolean completed, File file);
}
