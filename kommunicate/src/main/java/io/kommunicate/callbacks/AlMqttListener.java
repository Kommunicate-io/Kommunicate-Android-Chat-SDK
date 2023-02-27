package io.kommunicate.callbacks;

import io.kommunicate.models.feed.MqttMessageResponse;

public interface AlMqttListener {
    void onMqttMessageReceived(MqttMessageResponse mqttMessage);
}
