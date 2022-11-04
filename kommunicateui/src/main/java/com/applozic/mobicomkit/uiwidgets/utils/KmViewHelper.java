package com.applozic.mobicomkit.uiwidgets.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.TextView;

import com.applozic.mobicomkit.uiwidgets.alphanumbericcolor.AlphaNumberColorUtil;
import com.applozic.mobicommons.people.contact.Contact;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import androidx.annotation.Nullable;
import de.hdodenhof.circleimageview.CircleImageView;

public class KmViewHelper {

    public static void loadContactImage(Context context, CircleImageView imageView, TextView textView, Contact contact, int placeholderImage) {
        try {
            textView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            String contactNumber = "";
            char firstLetter = 0;
            contactNumber = contact.getDisplayName().toUpperCase();
            firstLetter = contact.getDisplayName().toUpperCase().charAt(0);

            if (firstLetter != '+') {
                textView.setText(String.valueOf(firstLetter));
            } else if (contactNumber.length() >= 2) {
                textView.setText(String.valueOf(contactNumber.charAt(1)));
            }

            Character colorKey = AlphaNumberColorUtil.alphabetBackgroundColorMap.containsKey(firstLetter) ? firstLetter : null;
            GradientDrawable bgShape = (GradientDrawable) textView.getBackground();
            bgShape.setColor(context.getResources().getColor(AlphaNumberColorUtil.alphabetBackgroundColorMap.get(colorKey)));

            if (contact.isDrawableResources()) {
                textView.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                int drawableResourceId = context.getResources().getIdentifier(contact.getrDrawableName(), "drawable", context.getPackageName());
                imageView.setImageResource(drawableResourceId);
            } else if (contact.getImageURL() != null) {
                loadImage(context, imageView, textView, contact.getImageURL(), placeholderImage);
            } else {
                textView.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadImage(Context context, final CircleImageView imageView, final TextView textImage, String imageUrl, int placeholderImage) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(placeholderImage)
                .error(placeholderImage);


        Glide.with(context).load(imageUrl).apply(options).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                if (textImage != null) {
                    textImage.setVisibility(View.VISIBLE);
                }
                imageView.setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                if (textImage != null) {
                    textImage.setVisibility(View.GONE);
                }
                imageView.setVisibility(View.VISIBLE);
                return false;
            }
        }).into(imageView);
    }
}
