package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.v2;

import android.text.TextUtils;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicommons.json.GsonUtils;

import java.util.Map;

public class KmAutoSuggestion {
    private String placeholder;
    private Object source;

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public Object getSource() {
        return source;
    }

    public void setSource(Object source) {
        this.source = source;
    }

    public static KmAutoSuggestion parseAutoSuggestion(Message message) {
        if (!TextUtils.isEmpty(message.getMetadata().get(Message.AUTO_SUGGESTION_TYPE_MESSAGE))) {
            return (KmAutoSuggestion) GsonUtils.getObjectFromJson(message.getMetadata().get(Message.AUTO_SUGGESTION_TYPE_MESSAGE), KmAutoSuggestion.class);
        }
        return null;
    }

    public static class Source {
        private String searchKey;
        private String message;
        private String url;
        private Map<String, String> headers;

        public String getSearchKey() {
            return searchKey;
        }

        public void setSearchKey(String searchKey) {
            this.searchKey = searchKey;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
        public Map<String, String> getHeaders() {
            return headers;
        }
        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }

        @Override
        public String toString() {
            return "Source{" +
                    "searchKey='" + searchKey + '\'' +
                    ", message='" + message + '\'' +
                    ", url='" + url + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "KmAutoSuggestion{" +
                "placeholder='" + placeholder + '\'' +
                ", source=" + source +
                '}';
    }
}
