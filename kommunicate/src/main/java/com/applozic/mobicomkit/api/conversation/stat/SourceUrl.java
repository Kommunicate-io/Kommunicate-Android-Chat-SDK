package com.applozic.mobicomkit.api.conversation.stat;

public class SourceUrl {

    public static final String SOURCE_URL = "answerSource";
    private String name;
    private String url;

    public SourceUrl(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getTitle() {
        return name;
    }

    public void setTitle(String title) {
        this.name = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "SourceUrl{name='" + name + "', url='" + url + "'}";
    }
}
