package io.kommunicate.devkit.api;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import java.util.concurrent.ScheduledExecutorService;

public class CoreMqttClient extends MqttClient {
    public CoreMqttClient(String serverURI, String clientId) throws MqttException {
        super(serverURI, clientId);
    }

    public CoreMqttClient(String serverURI, String clientId, MqttClientPersistence persistence) throws MqttException {
        super(serverURI, clientId, persistence);
    }

    public CoreMqttClient(String serverURI, String clientId, MqttClientPersistence persistence, ScheduledExecutorService executorService) throws MqttException {
        super(serverURI, clientId, persistence, executorService);
    }

    public IMqttToken connectWithResult(MqttConnectOptions options, IMqttActionListener callback) throws MqttSecurityException, MqttException {
        IMqttToken tok = aClient.connect(options, null, callback);
        tok.waitForCompletion(getTimeToWait());
        return tok;
    }

    public void publish(String topic, MqttMessage message, IMqttActionListener callback) throws MqttException,
            MqttPersistenceException {
        aClient.publish(topic, message, null, callback).waitForCompletion(getTimeToWait());
    }
}
