package io.kommunicate.uiwidgets.conversation.richmessaging.webview;

import android.content.Context;
import android.widget.Toast;

import io.kommunicate.uiwidgets.R;
import io.kommunicate.uiwidgets.kommunicate.views.KmToast;

/**
 * Created by ashish on 07/03/18.
 */

public class KmWebViewJsInterface {
    Context mContext;

    KmWebViewJsInterface(Context c) {
        mContext = c;
    }

    @android.webkit.JavascriptInterface
    public void success(long id, final String paymentId) {
        KmToast.success(mContext, mContext.getString(R.string.form_action_success), Toast.LENGTH_SHORT).show();
    }
}
