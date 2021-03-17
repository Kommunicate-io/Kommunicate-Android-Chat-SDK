package io.kommunicate;

import android.content.Context;
import android.os.Process;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.AlMqttClient;
import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.broadcast.AlEventManager;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.feed.MqttMessageResponse;
import com.applozic.mobicomkit.listners.AlMqttListener;
import com.applozic.mobicommons.commons.core.utils.Utils;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Date;

public class KmCustomEventManager extends MobiComKitClientService implements AlMqttListener, MqttCallback {
    private static final String TAG = "KmEventManager";
    private static final String AGENT_STATUS_LISTENER = "agentStatusListener";
    public static final int AGENT_AWAY_STATUS = 2;
    public static final int AGENT_ONLINE_STATUS = 3;
    public static final String AGENT_STATUS_TOPIC = "status-v2";
    private Context context;
    private static final String MQTT_ENCRYPTION_TOPIC = "encr-";
    private AlMqttClient client;
    private MemoryPersistence memoryPersistence;

    private static KmCustomEventManager kmCustomEventManager;

    private KmCustomEventManager(Context context) {
        this.context = context;
        memoryPersistence = new MemoryPersistence();
    }

    public static KmCustomEventManager getInstance(Context context) {
        if (kmCustomEventManager == null) {
            kmCustomEventManager = new KmCustomEventManager(context);
        }
        return kmCustomEventManager;
    }

    public void subscribeToAgentStatus() {
        AlEventManager.getInstance().registerMqttListener(AGENT_STATUS_LISTENER, this);
        subscribeToCustomTopic(AGENT_STATUS_TOPIC, false);
    }

    public void unsubscribeToAgentStatus() {
        AlEventManager.getInstance().unregisterMqttListener(AGENT_STATUS_LISTENER);
        unSubscribeToCustomTopic(AGENT_STATUS_TOPIC, false);
    }

    public void publishAgentStatus(int status, String message) {
        MobiComUserPreference userPreference = MobiComUserPreference.getInstance(context);
        publishCustomData(AGENT_STATUS_TOPIC,
                userPreference.getSuUserKeyString() + "," +
                        userPreference.getDeviceKeyString() + "," +
                        status +
                        message, false);
    }

    @Override
    public void onMqttMessageReceived(MqttMessageResponse mqttMessage) {
        Utils.printLog(context, "StatusTest", "Received message : " + mqttMessage);
    }

    @Override
    public void connectionLost(Throwable cause) {
        BroadcastService.sendUpdate(context, BroadcastService.INTENT_ACTIONS.MQTT_DISCONNECTED.toString());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    public interface AgentStatusListener {
        void onStatusChanged(String agentId, int status);
    }

    private MqttConnectOptions getConnectionOptions() {
        MobiComUserPreference userPreference = MobiComUserPreference.getInstance(context);
        String authToken = userPreference.getUserAuthToken();

        MqttConnectOptions connOpts = new MqttConnectOptions();

        if (!TextUtils.isEmpty(authToken)) {
            connOpts.setUserName(getApplicationKey(context));
            connOpts.setPassword(authToken.toCharArray());
        }
        connOpts.setConnectionTimeout(60);
        connOpts.setWill(AGENT_STATUS_TOPIC, (userPreference.getSuUserKeyString() + "," + userPreference.getDeviceKeyString() + "," + "0").getBytes(), 0, true);
        return connOpts;
    }

    private AlMqttClient connect() {
        String userId = MobiComUserPreference.getInstance(context).getUserId();
        try {
            if (TextUtils.isEmpty(userId)) {
                return client;
            }
            if (client == null) {
                client = new AlMqttClient(getMqttBaseUrl(), userId + "-" + new Date().getTime(), memoryPersistence);
            }

            if (!client.isConnected()) {
                Utils.printLog(context, TAG, "Connecting to mqtt...");
                client.setCallback(this);
                client.connectWithResult(getConnectionOptions(), new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Utils.printLog(context, TAG, "Mqtt Connection successfull to : " + client.getServerURI());
                        BroadcastService.sendUpdate(context, BroadcastService.INTENT_ACTIONS.MQTT_CONNECTED.toString());
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Utils.printLog(context, TAG, "Mqtt Connection failed");
                        BroadcastService.sendUpdate(context, BroadcastService.INTENT_ACTIONS.MQTT_DISCONNECTED.toString());
                    }
                });
            }
        } catch (MqttException e) {
            Utils.printLog(context, TAG, "Connecting already in progress.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return client;
    }

    public synchronized void publishCustomData(final String customTopic, final String data, final boolean useEncrypted) {
        try {
            final AlMqttClient client = connect();
            if (client == null || !client.isConnected()) {
                return;
            }
            MqttMessage message = new MqttMessage();
            message.setRetained(false);
            message.setPayload(data.getBytes());
            message.setQos(0);
            final String topic = (useEncrypted ? MQTT_ENCRYPTION_TOPIC : "") + customTopic + "-" + getApplicationKey(context);
            client.publish(topic, message, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Utils.printLog(context, TAG, "Sent data : " + data + " to topic : " + topic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Utils.printLog(context, TAG, "Error in sending data : " + data + " to topic : " + topic);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void subscribeToCustomTopic(String customTopic, boolean useEncrypted) {
        try {
            final MqttClient client = connect();
            if (client != null && client.isConnected()) {
                String topic = (useEncrypted ? MQTT_ENCRYPTION_TOPIC : "") + customTopic;
                Utils.printLog(context, TAG, "Subscribing to topic : " + topic);
                client.subscribe(topic, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void unSubscribeToCustomTopic(final String customTopic, final boolean useEncrypted) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (client == null || !client.isConnected()) {
                        return;
                    }
                    String topic = (useEncrypted ? MQTT_ENCRYPTION_TOPIC : "") + customTopic;
                    Utils.printLog(context, TAG, "UnSubscribing from topic : " + topic);
                    client.unsubscribe(topic);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
    }
}
