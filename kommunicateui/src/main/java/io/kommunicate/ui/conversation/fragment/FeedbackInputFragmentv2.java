package io.kommunicate.ui.conversation.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.kommunicate.ui.AlCustomizationSettings;
import io.kommunicate.ui.R;
import io.kommunicate.ui.kommunicate.utils.KmThemeHelper;
import io.kommunicate.commons.ApplozicService;
import io.kommunicate.commons.commons.core.utils.Utils;
import io.kommunicate.commons.file.FileUtils;
import io.kommunicate.commons.json.GsonUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FeedbackInputFragmentv2#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FeedbackInputFragmentv2 extends BottomSheetDialogFragment implements View.OnClickListener {
    private static final String TAG = "FeedbackInputFragmentV2";
    private KmThemeHelper themeHelper;

    private boolean isCurrentlyInDarkMode;

    private LinearLayout feedbackContainer;
    private ImageView oneStar, twoStar, threeStar, fourStar, fiveStar;
    private TextView tvTitle, tvFeedbackTitle;
    private EditText etComment;


    private Button btnSubmit, btnClose;
    private FeedbackSubmissionListener feedbackSubmissionListener;
    public static final int RATING_ONE_STAR = 1;
    public static final int RATING_TWO_STAR = 2;
    public static final int RATING_THREE_STAR = 3;
    public static final int RATING_FOUR_STAR = 4;
    public static final int RATING_FIVE_STAR = 5;


    // default colors for light and dark modes
    private final Pair<String, String> feedbackContainerColor = new Pair<>("#FFFFFF", "#1F1E1E");
    private final Pair<String, String> textViewTitleColor = new Pair<>("#1C1C1C", "#FFFFFF");
    private final Pair<String, String> editTextFeedbackCommentHintColor = new Pair<>("#67757E", "#919191");

    //using IntDef to replace enum
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({RATING_ONE_STAR, RATING_TWO_STAR, RATING_THREE_STAR, RATING_FOUR_STAR, RATING_FIVE_STAR})
    private @interface STAR_RATING {
    }

    private static final @STAR_RATING

    List<Integer> FEEDBACK_RATING_VALUES = Arrays.asList(RATING_ONE_STAR, RATING_TWO_STAR, RATING_THREE_STAR, RATING_FOUR_STAR, RATING_FIVE_STAR);

    @STAR_RATING
    private int ratingValue;

    @STAR_RATING
    private int getRatingValue() {
        return ratingValue;
    }

    private void setRatingValue(@STAR_RATING int ratingValue) {
        this.ratingValue = ratingValue;
    }


    public void setRatingSubmitListener(FeedbackSubmissionListener feedbackSubmissionListener) {
        this.feedbackSubmissionListener = feedbackSubmissionListener;
    }

    public static String getFragTag() {
        return TAG;
    }

    public FeedbackInputFragmentv2() {
        super();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (!FeedbackSubmissionListener.class.isAssignableFrom(MobiComConversationFragment.class)) {
            Utils.printLog(context, TAG, "Implement FeedbackSubmissionListener in your parent fragment.");
            throw new ClassCastException("Implement FeedbackFragmentListener in your parent fragment.");
        }
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

        if (tvTitle != null) {
            tvTitle.setTextColor(Color.parseColor(isCurrentlyInDarkMode ? textViewTitleColor.second : textViewTitleColor.first));
        }
        if (tvFeedbackTitle != null) {
            tvFeedbackTitle.setTextColor(Color.parseColor(isCurrentlyInDarkMode ? textViewTitleColor.second : textViewTitleColor.first));

        }

        if (etComment != null) {
            etComment.setBackgroundResource(isCurrentlyInDarkMode ? R.drawable.et_text_bg_v2 : R.drawable.edit_text_bg);
            etComment.setHintTextColor(Color.parseColor(isCurrentlyInDarkMode ? editTextFeedbackCommentHintColor.second : editTextFeedbackCommentHintColor.first));
            etComment.setTextColor(Color.parseColor(isCurrentlyInDarkMode ? textViewTitleColor.second : textViewTitleColor.first));
        }
    }

    public static FeedbackInputFragmentv2 newInstance(String param1, String param2) {
        FeedbackInputFragmentv2 fragment = new FeedbackInputFragmentv2();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);
        String jsonString = FileUtils.loadSettingsJsonFile(ApplozicService.getContext(getContext()));
        AlCustomizationSettings alCustomizationSettings;
        if (!TextUtils.isEmpty(jsonString)) {
            alCustomizationSettings = (AlCustomizationSettings) GsonUtils.getObjectFromJson(jsonString, AlCustomizationSettings.class);
        } else {
            alCustomizationSettings = new AlCustomizationSettings();
        }
        themeHelper = KmThemeHelper.getInstance(getContext(), alCustomizationSettings);
        isCurrentlyInDarkMode = themeHelper.isDarkModeEnabledForSDK();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_feedback_input_fragmentv2, container, false);
        oneStar = view.findViewById(R.id.iv_star_one);
        twoStar = view.findViewById(R.id.iv_star_two);
        threeStar = view.findViewById(R.id.iv_star_three);
        fourStar = view.findViewById(R.id.iv_star_four);
        fiveStar = view.findViewById(R.id.iv_star_five);
        btnClose = view.findViewById(R.id.btn_close);
        btnSubmit = view.findViewById(R.id.btn_submit);
        etComment = view.findViewById(R.id.et_feedback_comment);
        feedbackContainer = view.findViewById(R.id.feedback_container);
        tvTitle = view.findViewById(R.id.tv_feedback_title);
        tvFeedbackTitle = view.findViewById(R.id.tv_feedback);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String feedbackComment = etComment.getText().toString().trim();
                feedbackSubmissionListener.onFeedbackFragmentV2SubmitButtonPressed(getRatingValue(), feedbackComment);
                etComment.setText("");
                dismissAllowingStateLoss();
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissAllowingStateLoss();
            }
        });

        oneStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetStarImages();
                fillStar(1);
            }
        });
        twoStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetStarImages();
                fillStar(2);
            }
        });
        threeStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetStarImages();
                fillStar(3);
            }
        });
        fourStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetStarImages();
                fillStar(4);
            }
        });
        fiveStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetStarImages();
                fillStar(5);
            }
        });

        return view;
    }

    void resetStarImages() {
        oneStar.setImageDrawable(getResources().getDrawable(R.drawable.icon_empty_star));
        twoStar.setImageDrawable(getResources().getDrawable(R.drawable.icon_empty_star));
        threeStar.setImageDrawable(getResources().getDrawable(R.drawable.icon_empty_star));
        fourStar.setImageDrawable(getResources().getDrawable(R.drawable.icon_empty_star));
        fiveStar.setImageDrawable(getResources().getDrawable(R.drawable.icon_empty_star));
    }

    void fillStar(int number) {
        switch (number) {
            case 1:
                setRatingValue(RATING_ONE_STAR);
                oneStar.setImageDrawable(getResources().getDrawable(R.drawable.star));
                break;
            case 2:
                setRatingValue(RATING_TWO_STAR);
                oneStar.setImageDrawable(getResources().getDrawable(R.drawable.star));
                twoStar.setImageDrawable(getResources().getDrawable(R.drawable.star));
                break;
            case 3:
                setRatingValue(RATING_THREE_STAR);
                oneStar.setImageDrawable(getResources().getDrawable(R.drawable.star));
                twoStar.setImageDrawable(getResources().getDrawable(R.drawable.star));
                threeStar.setImageDrawable(getResources().getDrawable(R.drawable.star));
                break;
            case 4:
                setRatingValue(RATING_FOUR_STAR);
                oneStar.setImageDrawable(getResources().getDrawable(R.drawable.star));
                twoStar.setImageDrawable(getResources().getDrawable(R.drawable.star));
                threeStar.setImageDrawable(getResources().getDrawable(R.drawable.star));
                fourStar.setImageDrawable(getResources().getDrawable(R.drawable.star));
                break;
            case 5:
                setRatingValue(RATING_FIVE_STAR);
                oneStar.setImageDrawable(getResources().getDrawable(R.drawable.star));
                twoStar.setImageDrawable(getResources().getDrawable(R.drawable.star));
                threeStar.setImageDrawable(getResources().getDrawable(R.drawable.star));
                fourStar.setImageDrawable(getResources().getDrawable(R.drawable.star));
                fiveStar.setImageDrawable(getResources().getDrawable(R.drawable.star));
                break;

        }

    }

    @Override
    public void onClick(View v) {
//        if (v.getId() == vi)
    }


    public interface FeedbackSubmissionListener {
        void onFeedbackFragmentV2SubmitButtonPressed(int ratingValue, String feedback);
    }
}