package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.v2.KmAutoSuggestion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KmAutoSuggestionArrayAdapter<T> extends ArrayAdapter<T> {

    private ArrayList<T> fullList;
    private ArrayList<T> suggestions;
    private int layoutResourceId;

    public KmAutoSuggestionArrayAdapter(Context context, int layoutResourceId, T[] objects) {
        super(context, layoutResourceId, objects);
        this.layoutResourceId = layoutResourceId;
        fullList = new ArrayList<>(Arrays.asList(objects));
    }

    @Override
    public int getCount() {
        return fullList == null ? 0 : fullList.size();
    }

    @Override
    public T getItem(int position) {
        return fullList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layoutResourceId, parent, false);
        }

        TextView nameView = (TextView) convertView.findViewById(R.id.km_name_tv);
        T source = fullList.get(position);

        if (source instanceof String) {
            nameView.setText((String) source);
        } else if (source instanceof KmAutoSuggestion.Source) {
            nameView.setText(((KmAutoSuggestion.Source) source).getMessage());
        }

        return convertView;
    }

    @Override
    public Filter getFilter() {
        return nameFilter;
    }

    Filter nameFilter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            if (resultValue instanceof KmAutoSuggestion.Source) {
                return ((KmAutoSuggestion.Source) resultValue).getSearchKey();
            }
            return (String) resultValue;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            final FilterResults oReturn = new FilterResults();
            List<T> results = new ArrayList<>();

            if (suggestions == null) {
                suggestions = fullList;
            }

            if (constraint != null) {
                if (suggestions != null && suggestions.size() > 0) {
                    for (T data : suggestions) {
                        if ((data instanceof KmAutoSuggestion.Source ? ((KmAutoSuggestion.Source) data).getSearchKey() : (String) data).toLowerCase().contains(constraint.toString().toLowerCase())) {
                            results.add(data);
                        }
                    }
                }
                oReturn.values = results;
            } else {
                oReturn.values = suggestions;
            }
            return oReturn;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            fullList = (ArrayList<T>) results.values;
            notifyDataSetChanged();
        }
    };
}
