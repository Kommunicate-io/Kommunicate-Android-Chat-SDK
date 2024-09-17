package io.kommunicate.models;

import com.applozic.mobicommons.json.JsonMarker;

public class KmAutoSuggestionModel extends JsonMarker {
    private String name;
    private String type;
    private String category;
    private String content;
    private boolean deleted;
    private String created_at;
    private String updated_at;
    private String userName;
    private long id;
    private boolean supportsRichMessage = false;


    public boolean isSupportsRichMessage() {
        return supportsRichMessage;
    }
    public void setSupportsRichMessage(boolean SupportRichMessage) {
        this.supportsRichMessage = SupportRichMessage;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdatedAt() {
        return updated_at;
    }

    public void setUpdatedAt(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
