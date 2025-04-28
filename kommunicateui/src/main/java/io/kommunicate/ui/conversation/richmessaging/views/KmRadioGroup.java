package io.kommunicate.ui.conversation.richmessaging.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import io.kommunicate.ui.CustomizationSettings;
import io.kommunicate.ui.R;
import io.kommunicate.ui.conversation.richmessaging.models.v2.KmFormPayloadModel;
import io.kommunicate.ui.kommunicate.utils.KmThemeHelper;

import java.util.List;

public class KmRadioGroup {

    private KmFlowLayout flowLayout;
    private List<KmFormPayloadModel.Options> options;
    private Context context;
    private KmRadioButtonClickListener radioButtonClickListener;

    public KmRadioGroup(Context context, KmRadioButtonClickListener radioButtonClickListener, KmFlowLayout flowLayout, List<KmFormPayloadModel.Options> options) {
        this.flowLayout = flowLayout;
        this.options = options;
        this.context = context;
        this.radioButtonClickListener = radioButtonClickListener;
    }

    public void createLayout(int checkedIndex) {
        if (options != null && !options.isEmpty()) {
            flowLayout.setVisibility(View.VISIBLE);
            flowLayout.removeAllViews();
            for (final KmFormPayloadModel.Options option : options) {
                final int index = options.indexOf(option);
                RadioButton radioButton = new RadioButton(context);
                radioButton.setChecked(checkedIndex == index);
                radioButton.setGravity(Gravity.FILL);
                radioButton.setPaddingRelative(0, 0, 20, 0);

                radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        createLayout(index);
                        if (radioButtonClickListener != null) {
                            radioButtonClickListener.onClick(index);
                        }
                    }
                });
                radioButton.setText(option.getLabel());
                if (KmThemeHelper.getInstance(context,new CustomizationSettings()).isDarkModeEnabledForSDK()){
                    radioButton.setTextColor(context.getResources().getColor(R.color.white));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        radioButton.setButtonTintList(new ColorStateList(
                                new int[][] {
                                        new int[] {android.R.attr.state_checked},
                                        new int[] {android.R.attr.state_enabled}
                                },
                                new int[] {
                                        Color.WHITE,
                                        Color.WHITE
                                }
                        ));
                    }
                }

                flowLayout.addView(radioButton);
            }
        } else {
            flowLayout.setVisibility(View.GONE);
        }
    }

    public interface KmRadioButtonClickListener {
        void onClick(int index);
    }
}
