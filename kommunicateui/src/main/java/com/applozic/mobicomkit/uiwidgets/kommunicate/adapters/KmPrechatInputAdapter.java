package com.applozic.mobicomkit.uiwidgets.kommunicate.adapters;

import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.kommunicate.models.KmPrechatInputModel;

import java.util.List;

public class KmPrechatInputAdapter extends RecyclerView.Adapter<KmPrechatInputAdapter.KmPrechatInputViewHolder> {

    private List<KmPrechatInputModel> inputModelList;

    public KmPrechatInputAdapter(List<KmPrechatInputModel> inputModelList) {
        this.inputModelList = inputModelList;
    }

    @NonNull
    @Override
    public KmPrechatInputViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new KmPrechatInputViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.km_prechat_input_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull KmPrechatInputViewHolder holder, int position) {
        KmPrechatInputModel inputModel = inputModelList.get(position);

        if (inputModel != null) {
            holder.bind(inputModel);
        }
    }

    @Override
    public int getItemCount() {
        return inputModelList != null ? inputModelList.size() : 0;
    }

    public class KmPrechatInputViewHolder extends RecyclerView.ViewHolder {
        private EditText inputEditText;

        public KmPrechatInputViewHolder(@NonNull View itemView) {
            super(itemView);
            inputEditText = itemView.findViewById(R.id.prechatInputEt);
        }

        public void bind(KmPrechatInputModel inputModel) {
            if (inputModel != null) {
                inputEditText.setInputType(KmPrechatInputModel.KmInputType.getInputType(inputModel.getType()));
                inputEditText.setTransformationMethod(KmPrechatInputModel.KmInputType.PASSWORD.equals(inputModel.getType()) ? PasswordTransformationMethod.getInstance() : null);
                //inputEditText.setHint(inputModel.getField());
            }
        }
    }
}
