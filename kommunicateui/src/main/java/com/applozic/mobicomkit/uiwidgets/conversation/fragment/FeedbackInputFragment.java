package com.applozic.mobicomkit.uiwidgets.conversation.fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicommons.commons.core.utils.Utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * fragment for the feedback input form
 *
 * @author shubham
 * @date july 19
 */
public class FeedbackInputFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "FeedbackInputFragment";

    EditText editTextFeedbackComment;
    Button buttonSubmitFeedback;
    Button buttonCloseFragment;

    FeedbackRatingGroup feedbackRatingGroup;

    FeedbackFragmentListener feedbackFragmentListener;

    public static final int RATING_POOR = 1;
    public static final int RATING_AVERAGE = 2;
    public static final int RATING_GOOD = 3;

    //using IntDef to replace enum
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({RATING_POOR, RATING_AVERAGE, RATING_GOOD})
    public @interface Rating {
    }

    @Rating
    public int getRatingLevel() {
        return ratingLevel;
    }

    public void setRatingLevel(@Rating int ratingLevel) {
        this.ratingLevel = ratingLevel;
    }

    @Rating
    private int ratingLevel;

    public void setFeedbackFragmentListener(FeedbackFragmentListener feedbackFragmentListener) {
        this.feedbackFragmentListener = feedbackFragmentListener;
    }

    public static String getTAG() {
        return TAG;
    }

    public FeedbackInputFragment() {
        super();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!FeedbackFragmentListener.class.isAssignableFrom(MobiComConversationFragment.class)) {
            Utils.printLog(context, TAG, "Implement FeedbackFragmentListener in your parent fragment.");
            throw new ClassCastException("Implement FeedbackFragmentListener in your parent fragment.");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.feedback_rating_layout, container, false);

        editTextFeedbackComment = view.findViewById(R.id.idEditTextFeedback);
        buttonSubmitFeedback = view.findViewById(R.id.idButtonSubmit);

        buttonCloseFragment = view.findViewById(R.id.idCloseFeedbackFragment);

        editTextFeedbackComment.setVisibility(View.GONE);

        //selected
        Drawable[][] buttonDrawables = new Drawable[3][2];
        buttonDrawables[0][0] = getResources().getDrawable(R.drawable.ic_sad_1);
        buttonDrawables[1][0] = getResources().getDrawable(R.drawable.ic_confused);
        buttonDrawables[2][0] = getResources().getDrawable(R.drawable.ic_happy);
        //not selected
        buttonDrawables[0][1] = getResources().getDrawable(R.drawable.ic_sad_1_grey);
        buttonDrawables[1][1] = getResources().getDrawable(R.drawable.ic_confused_grey);
        buttonDrawables[2][1] = getResources().getDrawable(R.drawable.ic_happy_grey);

        feedbackRatingGroup = new FeedbackRatingGroup(3, buttonDrawables);

        feedbackRatingGroup.createViewForRatingValue(view, 1, R.id.idButtonPoor, R.id.idTextPoor);
        feedbackRatingGroup.createViewForRatingValue(view, 2, R.id.idButtonAverage, R.id.idTextAverage);
        feedbackRatingGroup.createViewForRatingValue(view, 3, R.id.idButtonGood, R.id.idTextGood);

        buttonSubmitFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ratingLevel == 0) {
                    setRatingLevel(RATING_AVERAGE);
                }
                String feedbackComment = editTextFeedbackComment.getText().toString().trim();
                feedbackFragmentListener.onFeedbackSubmitButtonPressed(getRatingLevel(), feedbackComment);
                getFragmentManager().popBackStack();
            }
        });

        buttonCloseFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStack();
            }
        });

        return view;
    }

    /**
     * toggle button as selected or deselected
     *
     * @param select         true or false
     * @param feedbackRating the views to toggle
     */
    void toggleRatingButtonSelected(boolean select, FeedbackRatingGroup.FeedbackRating feedbackRating) {
        //set drawable
        feedbackRating.selectDrawable(select);

        //set visibility and size
        if (select) {
            feedbackRating.ratingButton.setScaleX(1f);
            feedbackRating.ratingButton.setScaleY(1f);
            feedbackRating.feedbackTextView.setVisibility(View.VISIBLE);
        } else {
            feedbackRating.ratingButton.setScaleX(0.8f);
            feedbackRating.ratingButton.setScaleY(0.8f);
            feedbackRating.feedbackTextView.setVisibility(View.GONE);
        }
    }

    //when one of the rating buttons is clicked
    @Override
    public void onClick(View view) {
        Integer buttonTag = (Integer) view.getTag();
        setRatingLevel(buttonTag);

        //show the feedback comment input edit text, if not already visible
        if (editTextFeedbackComment.getVisibility() == View.GONE) {
            editTextFeedbackComment.setVisibility(View.VISIBLE);
        }

        for (FeedbackRatingGroup.FeedbackRating feedbackRating : feedbackRatingGroup.feedbackRating) {
            Integer iterationButtonTag = (Integer) feedbackRating.ratingButton.getTag();
            toggleRatingButtonSelected(iterationButtonTag.intValue() == buttonTag.intValue(), feedbackRating);
        }
    }

    /**
     * class for the feedback rating input buttons and text views
     */
    public class FeedbackRatingGroup {
        Drawable[][] drawables;
        FeedbackRating[] feedbackRating;
        int noOfRatingElements;

        public FeedbackRatingGroup(int noOfRatingElements, Drawable[][] drawables) {
            this.noOfRatingElements = noOfRatingElements;
            this.drawables = drawables;
            feedbackRating = new FeedbackRating[noOfRatingElements];
        }

        /**
         * class for a single rating button, text view and their properties
         */
        class FeedbackRating {
            Button ratingButton;
            TextView feedbackTextView;
            int ratingValue;

            void selectDrawable(boolean select) {
                if (select) {
                    ratingButton.setBackground(drawables[ratingValue - 1][0]);
                } else {
                    ratingButton.setBackground(drawables[ratingValue - 1][1]);
                }
            }
        }

        /**
         * this function will initialize a button and a text view with the properties provided and add it tp the
         * respective button and text view arrays
         * will also set a listener on it
         *
         * @param rootView      the parent root view
         * @param value         the rating value (work as index to array)
         * @param buttonResId   the id of the button in the layout file
         * @param textViewResId the id of the text view in the layout file
         */
        public void createViewForRatingValue(View rootView, int value, @IdRes int buttonResId, @IdRes int textViewResId) {
            FeedbackRating feedbackRatingObject = new FeedbackRating();

            feedbackRatingObject.ratingButton = rootView.findViewById(buttonResId);
            feedbackRatingObject.feedbackTextView = rootView.findViewById(textViewResId);

            feedbackRatingObject.ratingButton.setTag(value);
            feedbackRatingObject.ratingButton.setOnClickListener(FeedbackInputFragment.this);
            feedbackRatingObject.ratingButton.setScaleX(0.8f);
            feedbackRatingObject.ratingButton.setScaleY(0.8f);

            feedbackRatingObject.ratingValue = value;

            feedbackRatingObject.feedbackTextView.setVisibility(View.GONE);

            feedbackRating[value - 1] = feedbackRatingObject;
        }
    }

    public interface FeedbackFragmentListener {
        void onFeedbackSubmitButtonPressed(int rating, String feedback);
    }
}
