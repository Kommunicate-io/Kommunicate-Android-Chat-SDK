package io.kommunicate.ui.conversation.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import io.kommunicate.devkit.broadcast.EventManager;
import io.kommunicate.ui.CustomizationSettings;
import io.kommunicate.ui.R;
import io.kommunicate.ui.kommunicate.utils.KmThemeHelper;
import io.kommunicate.commons.AppContextService;
import io.kommunicate.commons.commons.core.utils.Utils;
import io.kommunicate.commons.file.FileUtils;
import io.kommunicate.commons.json.GsonUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;

/**
 * fragment for the feedback input form.
 *
 * @author shubham
 * @date july '19
 */
public class FeedbackInputFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    private static final String TAG = "FeedbackInputFragment";
    private static final String FeedbackFragmentListener = "Implement FeedbackFragmentListener in your parent fragment.";
    private EditText editTextFeedbackComment;
    private Button buttonSubmitFeedback;
    private Button buttonCloseFragment;

    private FeedbackRatingGroup feedbackRatingGroup;

    private FeedbackFragmentListener feedbackFragmentListener;
    private KmThemeHelper themeHelper;

    private boolean isCurrentlyInDarkMode;
    private LinearLayout feedbackContainer;
    private TextView textViewTitle, textViewPoor, textViewAverage, textViewGood;


    public static final int RATING_POOR = 1;
    public static final int RATING_AVERAGE = 5;
    public static final int RATING_GOOD = 10;

    // default colors for light and dark modes
    private final Pair<String, String> feedbackContainerColor = new Pair<>("#FFFFFF", "#1F1E1E");
    private final Pair<String, String> textViewTitleColor = new Pair<>("#000000", "#FFFFFF");
    private final Pair<String, String> textViewRatingColor = new Pair<>("#808080", "#919191");
    private final Pair<String, String> editTextFeedbackCommentHintColor = new Pair<>("#808080", "#919191");

    //using IntDef to replace enum
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({RATING_POOR, RATING_AVERAGE, RATING_GOOD})
    private @interface Rating {
    }

    private static final @Rating
    List<Integer> FEEDBACK_RATING_VALUES = Arrays.asList(RATING_POOR, RATING_AVERAGE, RATING_GOOD);

    @Rating
    private int ratingValue;

    @Rating
    private int getRatingValue() {
        return ratingValue;
    }

    private void setRatingValue(@Rating int ratingValue) {
        this.ratingValue = ratingValue;
    }

    public void setFeedbackFragmentListener(FeedbackFragmentListener feedbackFragmentListener) {
        this.feedbackFragmentListener = feedbackFragmentListener;
    }

    public static String getFragTag() {
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
            throw new ClassCastException(FeedbackFragmentListener);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);
        String jsonString = FileUtils.loadSettingsJsonFile(AppContextService.getContext(getContext()));
        CustomizationSettings customizationSettings;
        if (!TextUtils.isEmpty(jsonString)) {
            customizationSettings = (CustomizationSettings) GsonUtils.getObjectFromJson(jsonString, CustomizationSettings.class);
        } else {
            customizationSettings = new CustomizationSettings();
        }
        themeHelper = KmThemeHelper.getInstance(getContext(), customizationSettings);
        isCurrentlyInDarkMode = themeHelper.isDarkModeEnabledForSDK();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        boolean newDarkModeStatus = themeHelper.isDarkModeEnabledForSDK();
        if (isCurrentlyInDarkMode != newDarkModeStatus) {
            isCurrentlyInDarkMode = newDarkModeStatus;
        }
        setupModes(isCurrentlyInDarkMode);
    }

    private void setupModes(boolean isCurrentlyInDarkMode) {
        if (feedbackContainer != null) {
            feedbackContainer.setBackgroundColor(Color.parseColor(isCurrentlyInDarkMode ? feedbackContainerColor.second : feedbackContainerColor.first));
        }
        if (textViewTitle != null) {
            textViewTitle.setTextColor(Color.parseColor(isCurrentlyInDarkMode ? textViewTitleColor.second : textViewTitleColor.first));
        }
        if (textViewPoor != null) {
            textViewPoor.setTextColor(Color.parseColor(isCurrentlyInDarkMode ? textViewRatingColor.second : textViewRatingColor.first));
        }
        if (textViewAverage != null) {
            textViewAverage.setTextColor(Color.parseColor(isCurrentlyInDarkMode ? textViewRatingColor.second : textViewRatingColor.first));
        }
        if (textViewGood != null) {
            textViewGood.setTextColor(Color.parseColor(isCurrentlyInDarkMode ? textViewRatingColor.second : textViewRatingColor.first));
        }
        if (editTextFeedbackComment != null) {
            editTextFeedbackComment.setBackgroundResource(isCurrentlyInDarkMode ? R.drawable.edit_text_bg_night : R.drawable.edit_text_bg);
            editTextFeedbackComment.setHintTextColor(Color.parseColor(isCurrentlyInDarkMode ? editTextFeedbackCommentHintColor.second : editTextFeedbackCommentHintColor.first));
            editTextFeedbackComment.setTextColor(Color.parseColor(isCurrentlyInDarkMode ? textViewTitleColor.second : textViewTitleColor.first));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.feedback_rating_layout, container, false);

        feedbackContainer = view.findViewById(R.id.idFeedbackContainer);
        textViewTitle = view.findViewById(R.id.idTextViewTitle);
        textViewPoor = view.findViewById(R.id.idTextPoor);
        textViewAverage = view.findViewById(R.id.idTextAverage);
        textViewGood = view.findViewById(R.id.idTextGood);
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

        feedbackRatingGroup.createViewForRatingLevel(view, FEEDBACK_RATING_VALUES.indexOf(RATING_POOR), R.id.idButtonPoor, R.id.idTextPoor);
        feedbackRatingGroup.createViewForRatingLevel(view, FEEDBACK_RATING_VALUES.indexOf(RATING_AVERAGE), R.id.idButtonAverage, R.id.idTextAverage);
        feedbackRatingGroup.createViewForRatingLevel(view, FEEDBACK_RATING_VALUES.indexOf(RATING_GOOD), R.id.idButtonGood, R.id.idTextGood);

        buttonSubmitFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String feedbackComment = editTextFeedbackComment.getText().toString().trim();
                feedbackFragmentListener.onFeedbackFragmentSubmitButtonPressed(getRatingValue(), feedbackComment);
                editTextFeedbackComment.setText("");
                dismissAllowingStateLoss();
            }
        });

        buttonCloseFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissAllowingStateLoss();
            }
        });

        setupModes(isCurrentlyInDarkMode);

        return view;
    }

    /**
     * toggle button as selected or deselected.
     *
     * @param select         true or false
     * @param feedbackRating the views to toggle
     */
    private void toggleRatingButtonSelected(boolean select, FeedbackRatingGroup.FeedbackRating feedbackRating) {
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
        setRatingValue(FEEDBACK_RATING_VALUES.get(buttonTag));
        EventManager.getInstance().sendOnRatingEmoticonsClick(FEEDBACK_RATING_VALUES.get(buttonTag));

        //show the feedback comment input edit text, if not already visible
        if (editTextFeedbackComment.getVisibility() == View.GONE) {
            editTextFeedbackComment.setVisibility(View.VISIBLE);
        }
        if (buttonSubmitFeedback.getVisibility() == View.GONE) {
            buttonSubmitFeedback.setVisibility(View.VISIBLE);
        }

        for (FeedbackRatingGroup.FeedbackRating feedbackRating : feedbackRatingGroup.feedbackRating) {
            Integer iterationButtonTag = (Integer) feedbackRating.ratingButton.getTag();
            toggleRatingButtonSelected(iterationButtonTag.intValue() == buttonTag.intValue(), feedbackRating);
        }
    }

    /**
     * class for the feedback rating input buttons and text views.
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
         * class for a single rating button, text view and their properties.
         */
        class FeedbackRating {
            Button ratingButton;
            TextView feedbackTextView;
            int ratingValue;

            void selectDrawable(boolean select) {
                int ratingIndex = FEEDBACK_RATING_VALUES.indexOf(ratingValue);
                if (select) {
                    ratingButton.setBackground(drawables[ratingIndex][0]);
                } else {
                    ratingButton.setBackground(drawables[ratingIndex][1]);
                }
            }
        }

        /**
         * this function will initialize a button and a text view with the properties provided and add it tp the
         * respective button and text view arrays
         * will also set a listener on it.
         *
         * @param rootView      the parent root view
         * @param index         the rating index (used to identify rating level and get value)
         * @param buttonResId   the id of the button in the layout file
         * @param textViewResId the id of the text view in the layout file
         */
        public void createViewForRatingLevel(View rootView, int index, @IdRes int buttonResId, @IdRes int textViewResId) {
            FeedbackRating feedbackRatingObject = new FeedbackRating();

            feedbackRatingObject.ratingButton = rootView.findViewById(buttonResId);
            feedbackRatingObject.feedbackTextView = rootView.findViewById(textViewResId);

            feedbackRatingObject.ratingButton.setTag(index);
            feedbackRatingObject.ratingButton.setOnClickListener(FeedbackInputFragment.this);
            feedbackRatingObject.ratingButton.setScaleX(0.8f);
            feedbackRatingObject.ratingButton.setScaleY(0.8f);

            feedbackRatingObject.ratingValue = FEEDBACK_RATING_VALUES.get(index);

            feedbackRatingObject.feedbackTextView.setVisibility(View.GONE);

            feedbackRating[index] = feedbackRatingObject;
        }
    }

    public interface FeedbackFragmentListener {
        void onFeedbackFragmentSubmitButtonPressed(int ratingValue, String feedback);
    }
}
