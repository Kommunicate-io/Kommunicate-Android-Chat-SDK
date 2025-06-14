package io.kommunicate.commons.call;

import android.content.Context;
import android.telephony.TelephonyManager;

import java.lang.reflect.Method;

/**
 * Created by devashish on 24/1/15.
 */
public class CallService {

    private static final String END_CALL = "endCall";
    private static final String GET_TELEPHONY = "getITelephony";
    public static void cancelCall(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            Class c = Class.forName(tm.getClass().getName());
            Method m = c.getDeclaredMethod(GET_TELEPHONY);
            m.setAccessible(true);
            Object telephonyService = m.invoke(tm); // Get the internal ITelephony object
            c = Class.forName(telephonyService.getClass().getName()); // Get its class
            m = c.getDeclaredMethod(END_CALL); // Get the "endCall()" method
            m.setAccessible(true); // Make it accessible
            m.invoke(telephonyService); // invoke endCall()
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
