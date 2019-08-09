package com.applozic.mobicomkit.uiwidgets.conversation.fragment;

import android.content.Context;
import android.os.Bundle;
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

/**
 * fragment for the feedback input form
 * @author shubham
 * @date july 19
 */
public class FeedbackInputFragment extends Fragment {

    private static final String TAG = "FeedbackInputFragment";

    EditText editTextFeedback;
    Button button1;
    Button button2;
    Button button3;
    TextView textView1;
    TextView textView2;
    TextView textView3;
    Button buttonSubmit;
    TextView textViewRestartConversation;

    FeedbackFragmentListener feedbackFragmentListener;
    static final int RATINGBAD = 1;
    static final int RATINGGOOD = 2;
    static final int RATINGGREAT = 3;
    private int ratingValue;

    public void setFeedbackFragmentListener(FeedbackFragmentListener feedbackFragmentListener) {
        this.feedbackFragmentListener = feedbackFragmentListener;
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

        editTextFeedback = view.findViewById(R.id.idEditTextFeedback);
        buttonSubmit = view.findViewById(R.id.idButtonSubmit);
        button1 = view.findViewById(R.id.idButton1);
        button2 = view.findViewById(R.id.idButton2);
        button3 = view.findViewById(R.id.idButton3);
        textView1 = view.findViewById(R.id.idText1);
        textView2 = view.findViewById(R.id.idText2);
        textView3 = view.findViewById(R.id.idText3);
        textViewRestartConversation = view.findViewById(R.id.idTextViewRestart);

        button1.setScaleX(0.8f);
        button2.setScaleX(0.8f);
        button3.setScaleX(0.8f);
        button1.setScaleY(0.8f);
        button2.setScaleY(0.8f);
        button3.setScaleY(0.8f);

        editTextFeedback.setVisibility(View.GONE);
        textView1.setVisibility(View.INVISIBLE);
        textView2.setVisibility(View.INVISIBLE);
        textView3.setVisibility(View.INVISIBLE);

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ratingValue == 0) {
                    ratingValue = RATINGGOOD;
                }
                String feedbackComment = editTextFeedback.getText().toString().trim();
                feedbackFragmentListener.onFeedbackSubmitButtonPressed(ratingValue, feedbackComment);
                getFragmentManager().popBackStack();
            }
        });

        textViewRestartConversation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStack();
                feedbackFragmentListener.onRestartConversationPressed();
            }
        });

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ratingValue = RATINGBAD;
                makeClickedButton(ratingValue);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ratingValue = RATINGGOOD;
                makeClickedButton(ratingValue);
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ratingValue = RATINGGREAT;
                makeClickedButton(ratingValue);
            }
        });
        return view;
    }

    //ui select for the particular option
    void showSingleRating(int ratingValue) {
        if(ratingValue == RATINGBAD) {

            textView1.setVisibility(View.VISIBLE);
            textView2.setVisibility(View.INVISIBLE);
            textView3.setVisibility(View.INVISIBLE);
            button1.setBackground(getResources().getDrawable(R.drawable.ic_sad_1));
            button2.setBackground(getResources().getDrawable(R.drawable.ic_confused_grey));
            button3.setBackground(getResources().getDrawable(R.drawable.ic_happy_grey));
            button1.setScaleX(1f);
            button1.setScaleY(1f);
            button2.setScaleX(0.8f);
            button2.setScaleY(0.8f);
            button3.setScaleX(0.8f);
            button3.setScaleY(0.8f);
            return;
        }
        if(ratingValue == RATINGGOOD) {
            textView1.setVisibility(View.INVISIBLE);
            textView2.setVisibility(View.VISIBLE);
            textView3.setVisibility(View.INVISIBLE);
            button1.setBackground(getResources().getDrawable(R.drawable.ic_sad_1_grey));
            button2.setBackground(getResources().getDrawable(R.drawable.ic_confused));
            button3.setBackground(getResources().getDrawable(R.drawable.ic_happy_grey));
            button1.setScaleX(0.8f);
            button2.setScaleY(0.8f);
            button3.setScaleX(1f);
            button1.setScaleY(1f);
            button2.setScaleX(0.8f);
            button3.setScaleY(0.8f);
            return;
        }
        if(ratingValue == RATINGGREAT) {
            textView1.setVisibility(View.INVISIBLE);
            textView2.setVisibility(View.INVISIBLE);
            textView3.setVisibility(View.VISIBLE);
            button1.setBackground(getResources().getDrawable(R.drawable.ic_sad_1_grey));
            button2.setBackground(getResources().getDrawable(R.drawable.ic_confused_grey));
            button3.setBackground(getResources().getDrawable(R.drawable.ic_happy));
            button1.setScaleX(0.8f);
            button1.setScaleY(0.8f);
            button2.setScaleX(0.8f);
            button2.setScaleY(0.8f);
            button3.setScaleX(1f);
            button3.setScaleY(1f);
            return;
        }
    }

    //when any of the 3 image buttons are clicked
    void makeClickedButton(int ratingValue) {
        editTextFeedback.setVisibility(View.VISIBLE);
        showSingleRating(ratingValue);
    }

    public interface FeedbackFragmentListener {
        void onFeedbackSubmitButtonPressed(int rating, String feedback);
        void onRestartConversationPressed();
    }
}
