package com.applozic.mobicomkit.uiwidgets.conversation.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.library.baseAdapters.BR;
import androidx.recyclerview.widget.RecyclerView;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.KmResolve;
import com.applozic.mobicomkit.uiwidgets.databinding.KmConversationStatusItemViewBinding;
import com.applozic.mobicomkit.uiwidgets.kommunicate.callbacks.KmClickHandler;

import java.util.List;

import io.kommunicate.utils.KmUtils;

public class KmConversationStatusListAdapter extends RecyclerView.Adapter<KmConversationStatusListAdapter.StatusViewHolder> {

    private List<KmResolve> data;
    private KmClickHandler<KmResolve> kmClickHandler;

    public KmConversationStatusListAdapter(List<KmResolve> data, KmClickHandler<KmResolve> kmClickHandler) {
        this.data = data;
        this.kmClickHandler = kmClickHandler;
    }

    @NonNull
    @Override
    public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StatusViewHolder((KmConversationStatusItemViewBinding) DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.km_conversation_status_item_view,
                parent,
                false));
    }

    @Override
    public void onBindViewHolder(@NonNull StatusViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class StatusViewHolder extends RecyclerView.ViewHolder {
        private final KmConversationStatusItemViewBinding binding;

        public StatusViewHolder(KmConversationStatusItemViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.setClickHandler(kmClickHandler);
        }

        public void bind(KmResolve obj) {
            binding.setVariable(BR.itemResolveModel, obj);
            binding.executePendingBindings();
            KmUtils.setDrawableTint(binding.conversationStatusTextView, obj.getIconTintColorId() == 0 ? obj.getColorResId() : obj.getIconTintColorId(), 0);
        }
    }
}
