package io.kommunicate.devkit.listners;

import io.kommunicate.devkit.feed.MqttMessageResponse;

public interface MqttListener {
    void onMqttMessageReceived(MqttMessageResponse mqttMessage);
}
