package io.kommunicate.ui.conversation.home;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.kommunicate.commons.json.JsonMarker;

/**
 * A card of the News tab, read from the "whatsNewList" array of the settings file. The field names
 * match the "whatsNewList" option of the web widget.
 */
public class KmWhatsNewItem extends JsonMarker {
    private String title;
    private String meta;
    private String description;
    private String link;
    private String linkText;

    /**
     * Cards shown when the settings file has no "whatsNewList". Same as the iOS SDK's defaults.
     */
    public static final List<KmWhatsNewItem> DEFAULT_LIST = Collections.unmodifiableList(Arrays.asList(
            new KmWhatsNewItem(
                    "October 2025 Product Release Notes",
                    "October 2025",
                    "2FA, bulk delete, search URLs, and more.",
                    "https://www.kommunicate.io/blog/product-release-notes-october-2025/",
                    "Read release notes"),
            new KmWhatsNewItem(
                    "Brash Solutions automated 500 daily queries with Kommunicate",
                    "Customer story",
                    "Learn how they diverted phone calls to AI chatbots and scaled support.",
                    "https://www.kommunicate.io/blog/brash-solutions/",
                    "Read the case study")
    ));

    public KmWhatsNewItem() {
    }

    private KmWhatsNewItem(String title, String meta, String description, String link, String linkText) {
        this.title = title;
        this.meta = meta;
        this.description = description;
        this.link = link;
        this.linkText = linkText;
    }

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
