package com.applozic.mobicomkit.uiwidgets.instruction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.view.Gravity;
import android.widget.Toast;

import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.broadcast.BroadcastService;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by devashish on 25/9/14.
 */
public class InstructionUtil {

    public static final String SHARED_PREFERENCE_INSTRUCTION_KEY = "mck.instruction";

    private final static Map<Integer, Toast> toastMap = new HashMap<Integer, Toast>();
    private static final String info_message_sync = "info_message_sync";
    private static final String instruction_open_conversation_thread = "instruction_open_conversation_thread";
    private static final String instruction_go_back_to_recent_conversation_list = "instruction_go_back_to_recent_conversation_list";
    private static final String instruction_long_press_message = "instruction_long_press_message";
    public static boolean enabled = true;

    public static void init(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MobiComKitClientService.getApplicationKey(context), Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(SHARED_PREFERENCE_INSTRUCTION_KEY + "." + info_message_sync, true).commit();
        sharedPreferences.edit().putBoolean(SHARED_PREFERENCE_INSTRUCTION_KEY + "." + instruction_open_conversation_thread, true).commit();
        sharedPreferences.edit().putBoolean(SHARED_PREFERENCE_INSTRUCTION_KEY + "." + instruction_go_back_to_recent_conversation_list, true).commit();
        sharedPreferences.edit().putBoolean(SHARED_PREFERENCE_INSTRUCTION_KEY + "." + instruction_long_press_message, true).commit();
    }

    public static void showInstruction(final Context context, final int resId, int delay, String action) {
        showInstruction(context, resId, delay, true, action);
    }

    public static void showInstruction(final Context context, final int resId, int delay, final boolean actionable, final String action) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intentUpdate = new Intent();
                intentUpdate.setAction(action);
                intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
                intentUpdate.putExtra("resId", resId);
                intentUpdate.putExtra("actionable", actionable);
                BroadcastService.sendBroadcast(context, intentUpdate);
            }
        }, delay);
    }

    public static void showToast(Context context, int resId, int colorId) {
        Toast toast = Toast.makeText(context, context.getString(resId), Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.getView().setBackgroundColor(context.getResources().getColor(colorId));
        toast.show();
    }

    public static void showInstruction(Context context, int resId, boolean actionable, int colorId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MobiComKitClientService.getApplicationKey(context), Context.MODE_PRIVATE);
        if (!sharedPreferences.contains(SHARED_PREFERENCE_INSTRUCTION_KEY + "." + resId)) {
            return;
        }

        Toast toast = Toast.makeText(context, context.getString(resId), Toast.LENGTH_LONG);
        if (actionable) {
            toast.getView().setBackgroundColor(context.getResources().getColor(colorId));
        }

        toast.setGravity(Gravity.CENTER, 0, 0);

        if (!enabled) {
            return;
        }

        toast.show();

        sharedPreferences.edit().remove(SHARED_PREFERENCE_INSTRUCTION_KEY + "." + resId).commit();
        toastMap.put(resId, toast);
    }

    public static void showInfo(final Context context, final int resId, String action) {
        showInstruction(context, resId, 0, false, action);
    }

    public static void hideInstruction(Context context, int resId) {
        if (toastMap.get(resId) != null) {
            toastMap.get(resId).cancel();
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(MobiComKitClientService.getApplicationKey(context), Context.MODE_PRIVATE);
        sharedPreferences.edit().remove(SHARED_PREFERENCE_INSTRUCTION_KEY + "." + resId).commit();
    }
}
