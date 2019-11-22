package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.webview;

import android.content.Context;
import android.widget.Toast;

import com.applozic.mobicomkit.uiwidgets.R;

/**
 * Created by ashish on 07/03/18.
 */

public class AlWebViewJsInterface {
    Context mContext;

    AlWebViewJsInterface(Context c) {
        mContext = c;
    }

    @android.webkit.JavascriptInterface
    public void success(long id, final String paymentId) {
        Toast.makeText(mContext, mContext.getString(R.string.form_action_success), Toast.LENGTH_SHORT).show();
    }
}
