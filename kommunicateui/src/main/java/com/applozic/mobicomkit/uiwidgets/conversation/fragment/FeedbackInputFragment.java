package com.applozic.mobicomkit.uiwidgets.conversation.fragment;

import android.content.Context;
import android.os.Bundle;
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
 * @author shubham
 * @date july 19
 */
public class FeedbackInputFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = "FeedbackInputFragment";

    EditText editTextFeedbackComment;
    Button buttonsRating[];
    TextView textViewsRatingText[];
    Button buttonSubmitFeedback;
    Button buttonCloseFragment;

    FeedbackFragmentListener feedbackFragmentListener;

    public static final int RATING_POOR = 1;
    public static final int RATING_AVERAGE = 2;
    public static final int RATING_GOOD = 3;

    //using IntDef to replace enum
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({RATING_POOR, RATING_AVERAGE, RATING_GOOD})
    public @interface Rating {}

    @Rating
    public int getRatingLevel() {
        return ratingLevel;
    }

    public void setRatingLevel(@Rating int ratingLevel) {
        this.ratingLevel = ratingLevel;
    }

    @Rating private int ratingLevel;

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

        buttonsRating = new Button[3];
        textViewsRatingText = new TextView[3];

        buttonsRating[0] = view.findViewById(R.id.idButtonPoor);
        buttonsRating[1] = view.findViewById(R.id.idButtonAverage);
        buttonsRating[2] = view.findViewById(R.id.idButtonGood);
        textViewsRatingText[0] = view.findViewById(R.id.idTextPoor);
        textViewsRatingText[1] = view.findViewById(R.id.idTextAverage);
        textViewsRatingText[2] = view.findViewById(R.id.idTextGood);
        buttonCloseFragment = view.findViewById(R.id.idCloseFeedbackFragment);

        editTextFeedbackComment.setVisibility(View.GONE);
        textViewsRatingText[0].setVisibility(View.INVISIBLE);
        textViewsRatingText[1].setVisibility(View.INVISIBLE);
        textViewsRatingText[2].setVisibility(View.INVISIBLE);

        buttonSubmitFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ratingLevel == 0) {
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

        //set the un-selected size of the buttons to 80% of size
        buttonsRating[0].setScaleX(0.8f);
        buttonsRating[0].setScaleY(0.8f);
        buttonsRating[1].setScaleX(0.8f);
        buttonsRating[1].setScaleY(0.8f);
        buttonsRating[2].setScaleX(0.8f);
        buttonsRating[2].setScaleY(0.8f);

        buttonsRating[0].setOnClickListener(this);
        buttonsRating[1].setOnClickListener(this);
        buttonsRating[2].setOnClickListener(this);

        return view;
    }

    /**
     * toggle button as selected or deselected
     * @param select true or false
     * @param b the button to toggle
     */
    void toggleRatingButtonSelected(boolean select, Button b) {
        //if show the feedback comment input edit text, if not already visible
        if(editTextFeedbackComment.getVisibility()==View.GONE) {
            editTextFeedbackComment.setVisibility(View.VISIBLE);
        }

        //default initialization
        TextView t = textViewsRatingText[1];

        int buttonId = b.getId();

        //set drawable
        if (buttonId == R.id.idButtonPoor) {
            t = textViewsRatingText[0];
            if(select)
                b.setBackground(getResources().getDrawable(R.drawable.ic_sad_1));
            else
                b.setBackground(getResources().getDrawable(R.drawable.ic_sad_1_grey));
        }
        else if (buttonId == R.id.idButtonAverage) {
            t = textViewsRatingText[1];
            if(select)
                b.setBackground(getResources().getDrawable(R.drawable.ic_confused));
            else
                b.setBackground(getResources().getDrawable(R.drawable.ic_confused_grey));
        }
        else if (buttonId == R.id.idButtonGood) {
            t = textViewsRatingText[2];
            if(select)
                b.setBackground(getResources().getDrawable(R.drawable.ic_happy));
            else
                b.setBackground(getResources().getDrawable(R.drawable.ic_happy_grey));
        }

        //set visibility and size
        if(select) {
            b.setScaleX(1f);
            b.setScaleY(1f);
            t.setVisibility(View.VISIBLE);
        } else {
            b.setScaleX(0.8f);
            b.setScaleY(0.8f);
            t.setVisibility(View.GONE);
        }
    }

    //when one of the three rating buttons are clicked
    @Override
    public void onClick(View view) {
        int buttonId = view.getId();

        //counter variable for setting rating value
        int i = 0;
        for(Button button : buttonsRating) {
            if(button.getId()==buttonId) {
                setRatingLevel(i + 1);
                toggleRatingButtonSelected(true, button);
            }
            else
                toggleRatingButtonSelected(false, button);
            i++;
        }
    }

    public interface FeedbackFragmentListener {
        void onFeedbackSubmitButtonPressed(int rating, String feedback);
    }
}
