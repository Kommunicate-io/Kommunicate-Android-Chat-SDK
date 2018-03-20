package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.payment;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by ashish on 07/03/18.
 */

public class PaymentJsInterface {
    Context mContext;

    PaymentJsInterface(Context c) {
        mContext = c;
    }

    @android.webkit.JavascriptInterface
    public void success(long id, final String paymentId) {
        Toast.makeText(mContext, "Payment Successful...", Toast.LENGTH_SHORT).show();
    }
}
