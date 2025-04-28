package io.kommunicate.devkit.sync;

import io.kommunicate.commons.json.JsonMarker;
import io.kommunicate.devkit.api.conversation.Message;

import java.util.List;

public class SmsSyncRequest extends JsonMarker {

    private List<Message> smsList;

    public List<Message> getSmsList() {
        return smsList;
    }

    public void setSmsList(List<Message> smsList) {
        this.smsList = smsList;
    }


}


