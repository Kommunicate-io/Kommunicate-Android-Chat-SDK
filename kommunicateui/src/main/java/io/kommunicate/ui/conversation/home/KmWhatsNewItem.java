package io.kommunicate.ui.conversation.home;

/**
 * A card of the News tab, read from the "whatsNewList" array of the settings file. The field names
 * match the "whatsNewList" option of the web widget.
 */
public class KmWhatsNewItem {
    private String title;
    private String meta;
    private String description;
    private String link;
    private String linkText;

    public String getTitle() {
        return title;
    }

    /**
     * Small uppercase label shown under the title, for example a date or a category.
     */
    public String getMeta() {
        return meta;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Only http, https and mailto links are opened.
     */
    public String getLink() {
        return link;
    }

    public String getLinkText() {
        return linkText;
    }
}
