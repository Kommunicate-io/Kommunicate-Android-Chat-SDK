package com.applozic.mobicomkit.api.authentication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.listners.AlCallback;
import com.applozic.mobicommons.task.AlTask;

public class AlAuthService {

    public static boolean isTokenValid(long createdAtTime, int validUptoMins) {
        return (System.currentTimeMillis() - createdAtTime) / 60000 < validUptoMins;
    }

    public static void refreshToken(Context context, AlCallback callback) {
        AlTask.execute(new RefreshAuthTokenTask(context, callback));
    }

    public static boolean isTokenValid(Context context) {
        if (context == null) {
            return true;
        }

        MobiComUserPreference userPreference = MobiComUserPreference.getInstance(context);
        if (userPreference == null) {
            return true;
        }
        String token = userPreference.getUserAuthToken();
        long createdAtTime = userPreference.getTokenCreatedAtTime();
        int validUptoMins = userPreference.getTokenValidUptoMins();

        if ((validUptoMins > 0 && !isTokenValid(createdAtTime, validUptoMins)) || TextUtils.isEmpty(token)) {
            return false;
        } else if (!TextUtils.isEmpty(token)) {
            if ((createdAtTime == 0 || validUptoMins == 0)) {
                JWT.parseToken(context, token);
                isTokenValid(context);
            }
        }
        return true;
    }

    public static void verifyToken(Context context, String loadingMessage, AlCallback callback) {
        if (context == null) {
            return;
        }

        MobiComUserPreference userPreference = MobiComUserPreference.getInstance(context);
        if (userPreference == null) {
            return;
        }
        String token = userPreference.getUserAuthToken();
        long createdAtTime = userPreference.getTokenCreatedAtTime();
        int validUptoMins = userPreference.getTokenValidUptoMins();

        if ((validUptoMins > 0 && !isTokenValid(createdAtTime, validUptoMins)) || TextUtils.isEmpty(token)) {
            refreshToken(context, loadingMessage, callback);
        } else if (!TextUtils.isEmpty(token)) {
            if ((createdAtTime == 0 || validUptoMins == 0)) {
                JWT.parseToken(context, token);
                verifyToken(context, loadingMessage, callback);
            }
            if (callback != null) {
                callback.onSuccess(true);
            }
        }
    }

    public static void refreshToken(final Context context, String loadingMessage, final AlCallback callback) {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity(context));
        progressDialog.setMessage(loadingMessage);
        progressDialog.setCancelable(false);
        progressDialog.show();

        refreshToken(context, new AlCallback() {
            @Override
            public void onSuccess(Object response) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    if(progressDialog != null && (!getActivity(context).isDestroyed())) {
                        progressDialog.dismiss();
                    }
                }
                if (progressDialog != null && (!getActivity(context).isFinishing())) {
                    progressDialog.dismiss();

                }
                if (callback != null) {
                    callback.onSuccess(response);
                }
            }

            @Override
            public void onError(Object error) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    if(progressDialog != null && (!getActivity(context).isDestroyed())) {
                        progressDialog.dismiss();
                    }
                }
                if (progressDialog != null && (!getActivity(context).isFinishing())) {
                    progressDialog.dismiss();
                }
                if (callback != null) {
                    callback.onSuccess(error);
                }
            }
        });
    }

    public static Activity getActivity(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }
}
