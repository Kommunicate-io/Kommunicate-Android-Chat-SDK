package io.kommunicate.devkit.listners;

import io.kommunicate.devkit.feed.MqttMessageResponse;

public interface AlMqttListener {
    void onMqttMessageReceived(MqttMessageResponse mqttMessage);
}
