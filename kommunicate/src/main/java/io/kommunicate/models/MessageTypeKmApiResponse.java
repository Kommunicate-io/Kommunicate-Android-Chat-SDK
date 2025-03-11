package io.kommunicate.models;

import dev.kommunicate.commons.json.JsonMarker;

public class MessageTypeKmApiResponse<T> extends JsonMarker {
    private String message;
    private T data;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
