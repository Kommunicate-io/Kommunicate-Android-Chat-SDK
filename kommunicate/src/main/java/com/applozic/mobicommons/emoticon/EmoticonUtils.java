package com.applozic.mobicommons.emoticon;

import android.content.Context;
import android.text.Spannable;

import com.applozic.mobicommons.commons.core.utils.Utils;


/**
 * Created by devashish on 26/1/15.
 */
public class EmoticonUtils {
    public static final Spannable.Factory spannableFactory = Spannable.Factory
            .getInstance();

    public static Spannable getSmiledText(Context context, CharSequence text, EmojiconHandler emojiHandler) {
        Spannable spannable = spannableFactory.newSpannable(text);
        if (emojiHandler != null) {
            emojiHandler.addEmojis(context, spannable, Utils.dpToPx(28));
        }
        return spannable;
    }
}
