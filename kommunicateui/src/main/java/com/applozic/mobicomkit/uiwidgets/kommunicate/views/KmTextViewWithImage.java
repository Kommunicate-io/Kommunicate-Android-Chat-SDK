package com.applozic.mobicomkit.uiwidgets.kommunicate.views;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import android.text.Spannable;
import android.text.style.ImageSpan;
import android.util.AttributeSet;

import com.applozic.mobicommons.commons.core.utils.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KmTextViewWithImage extends androidx.appcompat.widget.AppCompatTextView {

    private static final String DRAWABLE = "drawable";
    /**
     * Regex pattern that looks for embedded images of the format: [img src=imageName/]
     */
    public static final String PATTERN = "\\Q[img src=\\E([a-zA-Z0-9_]+?)\\Q/]\\E";


    public KmTextViewWithImage(Context context) {
        super(context);
    }

    public KmTextViewWithImage(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public KmTextViewWithImage(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        final Spannable spannable = getTextWithImages(getContext(), text, Utils.dpToPx(9), getLineHeight(), getCurrentTextColor());
        super.setText(spannable, BufferType.SPANNABLE);
    }

    private static Spannable getTextWithImages(Context context, CharSequence text, int width, int height, int colour) {
        final Spannable spannable = Spannable.Factory.getInstance().newSpannable(text);
        addImages(context, spannable, width, height, colour);
        return spannable;
    }

    private static boolean addImages(Context context, Spannable spannable, int width, int height, int colour) {
        final Pattern refImg = Pattern.compile(PATTERN);
        boolean hasChanges = false;

        final Matcher matcher = refImg.matcher(spannable);
        while (matcher.find()) {
            boolean set = true;
            for (ImageSpan span : spannable.getSpans(matcher.start(), matcher.end(), ImageSpan.class)) {
                if (spannable.getSpanStart(span) >= matcher.start()
                        && spannable.getSpanEnd(span) <= matcher.end()) {
                    spannable.removeSpan(span);
                } else {
                    set = false;
                    break;
                }
            }
            final String resName = spannable.subSequence(matcher.start(1), matcher.end(1)).toString().trim();
            final int id = context.getResources().getIdentifier(resName, DRAWABLE, context.getPackageName());
            if (set) {
                hasChanges = true;
                spannable.setSpan(makeImageSpan(context, id, width, height, colour),
                        matcher.start(),
                        matcher.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
        }
        return hasChanges;
    }

    /**
     * Create an ImageSpan for the given icon drawable. This also sets the image size and colour.
     * Works best with a white, square icon because of the colouring and resizing.
     *
     * @param context       The Android Context.
     * @param drawableResId A drawable resource Id.
     * @param width          The desired size (i.e. width and height) of the image icon in pixels.
     *                      Use the lineHeight of the TextView to make the image inline with the
     *                      surrounding text.
     * @param colour        The colour (careful: NOT a resource Id) to apply to the image.
     * @return An ImageSpan, aligned with the bottom of the text.
     */
    private static ImageSpan makeImageSpan(Context context, int drawableResId, int width, int height, int colour) {
        final Drawable drawable = context.getResources().getDrawable(drawableResId);
        drawable.mutate();
        drawable.setColorFilter(colour, PorterDuff.Mode.MULTIPLY);
        drawable.setBounds(0, 0, width, height);
        return new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
    }
}
