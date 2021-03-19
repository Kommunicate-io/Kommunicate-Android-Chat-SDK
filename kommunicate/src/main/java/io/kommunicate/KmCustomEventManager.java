package io.kommunicate;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.ApplozicMqttService;
import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.broadcast.AlEventManager;
import com.applozic.mobicomkit.feed.MqttMessageResponse;
import com.applozic.mobicomkit.listners.AlMqttListener;

public class KmCustomEventManager extends MobiComKitClientService implements AlMqttListener {
    private static final String TAG = "KmEventManager";
    private static final String AGENT_STATUS_LISTENER = "agentStatusListener";
    public static final int AGENT_AWAY_STATUS = 2;
    public static final int AGENT_ONLINE_STATUS = 3;
    public static final int AGENT_OFFLINE_STATUS = 0;
    public static final int USER_ONLINE_STATUS = 1;
    private Context context;
    private AgentStatusListener agentStatusListener;

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

    public void subscribeToAgentStatus(AgentStatusListener agentStatusListener) {
        this.agentStatusListener = agentStatusListener;
        AlEventManager.getInstance().registerMqttListener(AGENT_STATUS_LISTENER, this);
        ApplozicMqttService.getInstance(context).subscribeToSupportGroup(false);
    }

    public void unsubscribeToAgentStatus() {
        agentStatusListener = null;
        AlEventManager.getInstance().unregisterMqttListener(AGENT_STATUS_LISTENER);
        ApplozicMqttService.getInstance(context).unSubscribeToSupportGroup(false);
    }

    public void publishAgentStatus(int status, String message) {
        MobiComUserPreference userPreference = MobiComUserPreference.getInstance(context);
        ApplozicMqttService.getInstance(context).publishCustomData(ApplozicMqttService.SUPPORT_GROUP_TOPIC + getApplicationKey(context),
                userPreference.getSuUserKeyString() + "," +
                        userPreference.getDeviceKeyString() + "," +
                        status + ", " +
                        (!TextUtils.isEmpty(message) ? message : ""), false);
    }

    @Override
    public void onMqttMessageReceived(String topic, MqttMessageResponse mqttMessage) {
        if (ApplozicMqttService.NOTIFICATION_TYPE.USER_ONLINE_STATUS.getValue().equals(mqttMessage.getType())) {
            String message = (String) mqttMessage.getMessage();
            if (!TextUtils.isEmpty(message)) {
                String[] messageArray = message.split(",");
                if (agentStatusListener != null) {
                    agentStatusListener.onStatusChanged(messageArray[0], Integer.parseInt(messageArray[1]));
                }
            }
        }
    }

    public interface AgentStatusListener {
        void onStatusChanged(String agentId, int status);
    }
}
