package com.applozic.mobicomkit.uiwidgets.conversation.adapter;

import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.databinding.KmAssigneeItemLayoutBinding;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.people.contact.Contact;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.users.KmContact;

public class KmAssigneeListAdapter extends RecyclerView.Adapter<KmAssigneeListAdapter.KmAssigneeViewHolder> implements Filterable {

    private List<KmContact> assigneeList;
    private List<KmContact> originalList;
    private String assigneeId;
    private String searchText;
    private TextAppearanceSpan highlightTextSpan;
    public KmCallback callback;

    public KmAssigneeListAdapter(String assigneeId, KmCallback callback) {
        this.assigneeList = new ArrayList<>();
        this.assigneeId = assigneeId;
        this.highlightTextSpan = new TextAppearanceSpan(ApplozicService.getAppContext(), R.style.KmAssigneeNameBold);
        this.callback = callback;
    }

    @NonNull
    @Override
    public KmAssigneeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new KmAssigneeViewHolder((KmAssigneeItemLayoutBinding) DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.km_assignee_item_layout,
                parent,
                false));
    }

    public void addAssigneeList(List<KmContact> assigneeList) {
        if (assigneeList != null && !assigneeList.isEmpty()) {
            this.assigneeList.addAll(assigneeList);
            notifyDataSetChanged();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull KmAssigneeViewHolder holder, int position) {
        holder.bind(assigneeList.get(position));
    }

    @Override
    public int getItemCount() {
        return assigneeList == null ? 0 : assigneeList.size();
    }

    public class KmAssigneeViewHolder extends RecyclerView.ViewHolder {
        private KmAssigneeItemLayoutBinding binding;

        public KmAssigneeViewHolder(@NonNull KmAssigneeItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Contact contact) {
            if (contact == null) {
                return;
            }

            String name = contact.getDisplayName();
            binding.kmNameTextView.setText(name);
            binding.setIsCurrentAssignee(contact.getUserId().equals(assigneeId));
            binding.setKmCallback(callback);
            binding.setContact(assigneeList.get(getAdapterPosition()));

            int startIndex = indexOfSearchQuery(contact.getDisplayName());
            if (startIndex != -1) {
                final SpannableString highlightedName = new SpannableString(name);
                highlightedName.setSpan(highlightTextSpan, startIndex, startIndex + searchText.length(), 0);
                binding.kmNameTextView.setText(highlightedName);
            }

            binding.executePendingBindings();
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                final FilterResults oReturn = new FilterResults();
                final List<KmContact> results = new ArrayList<>();
                if (originalList == null)
                    originalList = assigneeList;
                if (constraint != null) {
                    searchText = constraint.toString();
                    if (originalList != null && originalList.size() > 0) {
                        for (final KmContact contact : originalList) {
                            if (contact.getDisplayName().toLowerCase().contains(constraint.toString())) {
                                results.add(contact);
                            }
                        }
                    }
                    oReturn.values = results;
                } else {
                    oReturn.values = originalList;
                }
                return oReturn;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                assigneeList = (ArrayList<KmContact>) results.values;
                callback.onSuccess(assigneeList.isEmpty());
                notifyDataSetChanged();
            }
        };
    }

    private int indexOfSearchQuery(String name) {
        if (!TextUtils.isEmpty(searchText)) {
            return name.toLowerCase(Locale.getDefault()).indexOf(searchText.toLowerCase(Locale.getDefault()));
        }
        return -1;
    }
}
