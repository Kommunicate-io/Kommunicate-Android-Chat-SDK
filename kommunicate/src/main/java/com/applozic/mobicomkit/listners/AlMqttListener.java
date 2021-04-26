package com.applozic.mobicomkit.listners;

import com.applozic.mobicomkit.feed.MqttMessageResponse;

public interface AlMqttListener {
    void onMqttMessageReceived(MqttMessageResponse mqttMessage);
}
