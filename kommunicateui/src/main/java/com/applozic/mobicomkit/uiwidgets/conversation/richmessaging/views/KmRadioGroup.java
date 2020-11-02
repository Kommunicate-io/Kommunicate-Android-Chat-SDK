package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.views;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.v2.KmFormPayloadModel;

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
