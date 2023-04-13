package io.kommunicate;

import android.content.Context;

import io.kommunicate.data.api.KmMqttService;
import io.kommunicate.data.api.MobiComKitClientService;

public class KmCustomEventManager extends MobiComKitClientService {
    public static final int AGENT_AWAY_STATUS = 2;
    public static final int AGENT_ONLINE_STATUS = 3;
    private static final String TAG = "KmEventManager";
    private static KmCustomEventManager kmCustomEventManager;
    private Context context;

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
        KmMqttService.getInstance(context).publishCustomData(topic, data, useEncrypted);
    }

    public void subscribeToTopic(String topic, boolean useEncrypted) {
        KmMqttService.getInstance(context).subscribeToCustomTopic(topic, useEncrypted);
    }

    public void unSubscribeToTopic(String topic, boolean useEncrypted) {
        KmMqttService.getInstance(context).unSubscribeToCustomTopic(topic, useEncrypted);
    }
}
