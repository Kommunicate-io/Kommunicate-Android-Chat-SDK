package com.applozic.mobicomkit.uiwidgets.kommunicate.models;

import com.applozic.mobicommons.json.JsonMarker;

public class KmFontModel extends JsonMarker {
    private String messageTextFont;
    private String createdAtTimeFont;
    private String messageDisplayNameFont;
    private String toolbarTitleFont;
    private String toolbarSubtitleFont;
    private String messageEditTextFont;

    public String getMessageTextFont() {
        return messageTextFont;
    }

    public String getCreatedAtTimeFont() {
        return createdAtTimeFont;
    }

    public String getMessageDisplayNameFont() {
        return messageDisplayNameFont;
    }

    public String getToolbarTitleFont() {
        return toolbarTitleFont;
    }

    public String getToolbarSubtitleFont() {
        return toolbarSubtitleFont;
    }

    public String getMessageEditTextFont() {
        return messageEditTextFont;
    }
}
