package com.applozic.mobicomkit.uiwidgets.kommunicate.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.applozic.mobicomkit.uiwidgets.DimensionsUtils;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.FeedbackInputFragment;

import io.kommunicate.models.KmFeedback;

/**
 * fragment for the feedback display view.
 *
 * @author shubham
 * @date september '19
 */
public class KmFeedbackView extends LinearLayout {

    private static final String TAG = "KmFeedbackView";

    TextView textViewFeedbackComment;
    ImageView imageViewFeedbackRating;
    TextView textViewRestartConversation;
    ConstraintLayout constraintLayoutFeedbackTopLayout;
    LinearLayout rootLinearLayout;
    ScrollView scrollViewFeedbackCommentWrap;

    KmFeedbackViewCallbacks kmFeedbackViewCallbackListener;

    /**
     * inflate the view from the layout.
     *
     * @param context the activity context
     * @return the inflated linear layout
     */
    public LinearLayout inflateView(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        rootLinearLayout = (LinearLayout) layoutInflater.inflate(R.layout.feedback_display_layout, this, true);
        return rootLinearLayout;
    }

    public KmFeedbackView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(inflateView(context));
    }

    public KmFeedbackView(Context context) {
        super(context);
        init(inflateView(context));
    }

    public KmFeedbackView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(inflateView(context));
    }

    /**
     * initialize the view.
     *
     * @param view the root view passed
     */
    private void init(View view) {
        textViewRestartConversation = view.findViewById(R.id.idFeedbackRestartConversation);
        textViewFeedbackComment = view.findViewById(R.id.idFeedbackComment);
        imageViewFeedbackRating = view.findViewById(R.id.idRatingImage);
        constraintLayoutFeedbackTopLayout = view.findViewById(R.id.idFeedbackTopLayout);
        scrollViewFeedbackCommentWrap = view.findViewById(R.id.idCommentScrollView);

        textViewRestartConversation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                kmFeedbackViewCallbackListener.onRestartConversationPressed();
            }
        });

        //to set the max height of the scroll view to 70dp
        scrollViewFeedbackCommentWrap.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightInPixels = Math.round(DimensionsUtils.convertDpToPixel(70));
                if (scrollViewFeedbackCommentWrap.getHeight() > heightInPixels) {
                    LayoutParams layoutParams = (LayoutParams) scrollViewFeedbackCommentWrap.getLayoutParams();
                    layoutParams.height = heightInPixels;

                    scrollViewFeedbackCommentWrap.setLayoutParams(layoutParams);
                }
            }
        });
    }

    /**
     * set the feedback data and showFeedbackView the respective feedback layout views and viewgroups.
     *
     * @param context  the context
     * @param feedback the feedback object
     */
    public void showFeedback(Context context, KmFeedback feedback) {
        constraintLayoutFeedbackTopLayout.setVisibility(VISIBLE);

        int ratingValue = feedback.getRating();

        switch (ratingValue) {
            case FeedbackInputFragment.RATING_POOR:
                imageViewFeedbackRating.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_sad_1));
                break;
            case FeedbackInputFragment.RATING_AVERAGE:
                imageViewFeedbackRating.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_confused));
                break;
            case FeedbackInputFragment.RATING_GOOD:
                imageViewFeedbackRating.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_happy));
                break;
            default:
                imageViewFeedbackRating.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_confused));

        }

        if (feedback.getComments() != null && feedback.getComments().length > 0) {
            scrollViewFeedbackCommentWrap.setVisibility(VISIBLE);
            textViewFeedbackComment.setText("\"" + feedback.getComments()[feedback.getComments().length - 1] + "\"");
        }
    }

    public void setInteractionListener(KmFeedbackViewCallbacks kmFeedbackViewCallbackListener) {
        this.kmFeedbackViewCallbackListener = kmFeedbackViewCallbackListener;
    }

    //interface for callbacks
    public interface KmFeedbackViewCallbacks {
        void onRestartConversationPressed();
    }
}
