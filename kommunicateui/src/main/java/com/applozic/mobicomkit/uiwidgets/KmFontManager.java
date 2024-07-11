package com.applozic.mobicomkit.uiwidgets;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;

import com.applozic.mobicomkit.uiwidgets.kommunicate.models.KmFontModel;
import com.applozic.mobicommons.json.Exclude;
import com.applozic.mobicommons.json.JsonMarker;

public class KmFontManager extends JsonMarker {

    @Exclude
    private transient Context context;
    private static final String FONT_PATH = "fonts/";
    private Typeface messageTextFont;
    private Typeface createdAtTimeFont;
    private Typeface messageDisplayNameFont;
    private Typeface toolbarTitleFont;
    private Typeface toolbarSubtitleFont;
    private Typeface messageEditTextFont;

    public KmFontManager(Context context, AlCustomizationSettings alCustomizationSettings) {
        this.context = context;

        try {
            if (alCustomizationSettings != null) {
                KmFontModel fontModel = alCustomizationSettings.getFontModel();

                if (fontModel != null) {
                    messageTextFont = getTypeFace(fontModel.getMessageTextFont());
                    createdAtTimeFont = getTypeFace(fontModel.getCreatedAtTimeFont());
                    messageDisplayNameFont = getTypeFace(fontModel.getMessageDisplayNameFont());
                    toolbarTitleFont = getTypeFace(fontModel.getToolbarTitleFont());
                    toolbarSubtitleFont = getTypeFace(fontModel.getToolbarSubtitleFont());
                    messageEditTextFont = getTypeFace(fontModel.getMessageEditTextFont());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Typeface getMessageTextFont() {
        return messageTextFont;
    }

    public Typeface getCreatedAtTimeFont() {
        return createdAtTimeFont;
    }

    public Typeface getMessageDisplayNameFont() {
        return messageDisplayNameFont;
    }

    public Typeface getToolbarTitleFont() {
        return toolbarTitleFont;
    }

    public Typeface getToolbarSubtitleFont() {
        return toolbarSubtitleFont;
    }

    public Typeface getMessageEditTextFont() {
        return messageEditTextFont;
    }

    private Typeface getTypeFace(String fontPath) {
        if (!TextUtils.isEmpty(fontPath) && fontPath.startsWith(FONT_PATH)) {
            return Typeface.createFromAsset(context.getAssets(), fontPath);
        }
        return getDefaultTypeface(fontPath);
    }

    private Typeface getDefaultTypeface(String path) {
        if (!TextUtils.isEmpty(path)) {
            switch (path) {
                case DefaultFonts.NORMAL:
                    return Typeface.defaultFromStyle(Typeface.NORMAL);
                case DefaultFonts.BOLD:
                    return Typeface.defaultFromStyle(Typeface.BOLD);
                case DefaultFonts.ITALIC:
                    return Typeface.defaultFromStyle(Typeface.ITALIC);
                case DefaultFonts.BOLD_ITALIC:
                    return Typeface.defaultFromStyle(Typeface.BOLD_ITALIC);
                case DefaultFonts.DEFAULT:
                    return Typeface.DEFAULT;
                case DefaultFonts.DEFAULT_BOLD:
                    return Typeface.DEFAULT_BOLD;
                case DefaultFonts.MONOSPACE:
                    return Typeface.MONOSPACE;
                case DefaultFonts.SANS_SERIF:
                    return Typeface.SANS_SERIF;
                case DefaultFonts.SERIF:
                    return Typeface.SERIF;
                default:
                    return null;
            }
        }
        return null;
    }

    public static class DefaultFonts {
        public static final String NORMAL = "normal";
        public static final String BOLD = "bold";
        public static final String ITALIC = "italic";
        public static final String BOLD_ITALIC = "bold_italic";
        public static final String DEFAULT = "default";
        public static final String DEFAULT_BOLD = "default_bold";
        public static final String MONOSPACE = "monospace";
        public static final String SANS_SERIF = "sans_serif";
        public static final String SERIF = "serif";
    }
}
