package com.applozic.mobicomkit.uiwidgets.conversation.adapter;

/**
 * Created by devashish on 23/2/14.
 */

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.SpinnerNavItem;

import com.applozic.mobicommons.people.contact.Contact;

import java.util.ArrayList;

public class TitleNavigationAdapter extends BaseAdapter {

    private ImageView imgIcon;
    private TextView txtTitle;
    private TextView txtName;
    private ArrayList<SpinnerNavItem> spinnerNavItem;
    private Context context;

    public TitleNavigationAdapter(Context context,
                                  ArrayList<SpinnerNavItem> spinnerNavItem) {
        this.spinnerNavItem = spinnerNavItem;
        this.context = context;
    }

    @Override
    public int getCount() {
        return spinnerNavItem.size();
    }

    @Override
    public Object getItem(int index) {
        return spinnerNavItem.get(index);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.mobicom_list_item_title_navigation, parent, false);
        }

        imgIcon = (ImageView) convertView.findViewById(R.id.imgIcon);
        txtTitle = (TextView) convertView.findViewById(R.id.txtTitle);
        txtName = (TextView) convertView.findViewById(R.id.txtName);

        imgIcon.setImageResource(spinnerNavItem.get(position).getIcon());
        imgIcon.setVisibility(View.GONE);
        Contact contact = spinnerNavItem.get(position).getContact();
        txtName.setText(contact.getFullName() + " \r \n " + spinnerNavItem.get(position).getContactNumber());
        txtTitle.setVisibility(View.GONE);
        return convertView;
    }


    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.mobicom_list_item_title_navigation, parent, false);
        }

        imgIcon = (ImageView) convertView.findViewById(R.id.imgIcon);
        txtTitle = (TextView) convertView.findViewById(R.id.txtTitle);
        txtName = (TextView) convertView.findViewById(R.id.txtName);

        imgIcon.setImageResource(spinnerNavItem.get(position).getIcon());
        txtTitle.setText(spinnerNavItem.get(position).getType() + " " + spinnerNavItem.get(position).getContactNumber());
        txtName.setVisibility(View.GONE);
        return convertView;
    }

}
