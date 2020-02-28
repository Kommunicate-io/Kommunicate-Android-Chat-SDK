package com.applozic.mobicomkit.uiwidgets.kommunicate.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.databinding.KmAssigneeListLayoutBinding;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;

public class KmAssigneeListFragment extends Fragment {
    private static final String TAG = "KmAssigneeListFragment";
    private int status;
    private Channel channel;

    public static String getFragTag() {
        return TAG;
    }

    public static KmAssigneeListFragment newInstance() {
        KmAssigneeListFragment assigneeListFragment = new KmAssigneeListFragment();
        Bundle args = new Bundle();
        //args.putInt(Channel.CONVERSATION_STATUS, status);
        //args.putString(ConversationUIService.GROUP, GsonUtils.getJsonFromObject(channel, Channel.class));
        assigneeListFragment.setArguments(args);
        return assigneeListFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            status = bundle.getInt(Channel.CONVERSATION_STATUS, 0);
            channel = (Channel) GsonUtils.getObjectFromJson(bundle.getString(ConversationUIService.GROUP), Channel.class);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.km_assignee_list_layout, container, false);

        KmAssigneeListLayoutBinding binding = DataBindingUtil.bind(view.findViewById(R.id.kmAssigneeListLayout));

        if (binding != null) {
            binding.setAssigneeListFragment(this);
        }
        return view;
    }

    public void dismissFragment() {
        if (getFragmentManager() != null) {
            getFragmentManager().popBackStackImmediate();
        }
    }
}
