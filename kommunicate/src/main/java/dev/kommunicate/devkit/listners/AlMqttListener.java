package dev.kommunicate.devkit.listners;

import dev.kommunicate.devkit.feed.MqttMessageResponse;

public interface AlMqttListener {
    void onMqttMessageReceived(MqttMessageResponse mqttMessage);
}
