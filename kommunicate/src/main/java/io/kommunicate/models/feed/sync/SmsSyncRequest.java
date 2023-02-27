package io.kommunicate.models.feed.sync;

import java.util.List;

import io.kommunicate.data.conversation.Message;
import io.kommunicate.data.json.JsonMarker;

public class SmsSyncRequest extends JsonMarker {

    private List<Message> smsList;

    public List<Message> getSmsList() {
        return smsList;
    }

    public void setSmsList(List<Message> smsList) {
        this.smsList = smsList;
    }


}


