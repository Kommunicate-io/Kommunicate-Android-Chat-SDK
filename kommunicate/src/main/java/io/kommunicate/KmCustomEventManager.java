package io.kommunicate;

import android.content.Context;

import io.kommunicate.devkit.api.MqttService;
import io.kommunicate.devkit.api.MobiComKitClientService;

public class KmCustomEventManager extends MobiComKitClientService {
    private static final String TAG = "KmEventManager";
    public static final int AGENT_AWAY_STATUS = 2;
    public static final int AGENT_ONLINE_STATUS = 3;
    private Context context;

    private static KmCustomEventManager kmCustomEventManager;

    private KmCustomEventManager(Context context) {
        this.context = context;
    }

    public static KmCustomEventManager getInstance(Context context) {
        if (kmCustomEventManager == null) {
            kmCustomEventManager = new KmCustomEventManager(context);
        }
        return kmCustomEventManager;
    }

    public void publishDataToTopic(String topic, String data, boolean useEncrypted) {
        MqttService.getInstance(context).publishCustomData(topic, data, useEncrypted);
    }

    public void subscribeToTopic(String topic, boolean useEncrypted) {
        MqttService.getInstance(context).subscribeToCustomTopic(topic, useEncrypted);
    }

    public void unSubscribeToTopic(String topic, boolean useEncrypted) {
        MqttService.getInstance(context).unSubscribeToCustomTopic(topic, useEncrypted);
    }
}
