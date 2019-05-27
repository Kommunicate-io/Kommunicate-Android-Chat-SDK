package com.applozic.mobicomkit.uiwidgets.kommunicate.models;

import com.applozic.mobicommons.json.JsonMarker;

public class KmAutoSuggestionModel extends JsonMarker {
    private String name;
    private String type;
    private String category;
    private String content;
    private long id;

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public String getContent() {
        return content;
    }

    public long getId() {
        return id;
    }
}
