package dev.kommunicate.devkit.sync;

import dev.kommunicate.commons.json.JsonMarker;
import dev.kommunicate.devkit.api.conversation.Message;

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


