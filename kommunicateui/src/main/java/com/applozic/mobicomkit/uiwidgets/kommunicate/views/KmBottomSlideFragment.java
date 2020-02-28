package com.applozic.mobicomkit.uiwidgets.kommunicate.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.KmConversationStatus;
import com.applozic.mobicomkit.uiwidgets.conversation.KmResolve;
import com.applozic.mobicomkit.uiwidgets.conversation.adapter.KmConversationStatusListAdapter;
import com.applozic.mobicomkit.uiwidgets.databinding.KmConversationStatusListLayoutBinding;
import com.applozic.mobicomkit.uiwidgets.kommunicate.callbacks.KmClickHandler;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;

import java.util.ArrayList;
import java.util.List;


public class KmBottomSlideFragment extends Fragment implements KmClickHandler<KmResolve> {
    private static final String TAG = "KmBottomSlideFragment";
    private int status;
    private Channel channel;

    public static KmBottomSlideFragment newInstance(int status, Channel channel) {
        KmBottomSlideFragment bottomSlideFragment = new KmBottomSlideFragment();
        Bundle args = new Bundle();
        args.putInt(Channel.CONVERSATION_STATUS, status);
        args.putString(ConversationUIService.GROUP, GsonUtils.getJsonFromObject(channel, Channel.class));
        bottomSlideFragment.setArguments(args);
        return bottomSlideFragment;
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
        View view = inflater.inflate(R.layout.km_conversation_status_list_layout, container, false);

        KmConversationStatusListLayoutBinding binding = DataBindingUtil.bind(view.findViewById(R.id.kmStatusListLayout));

        if (binding != null) {
            binding.setBottomSlideFragment(this);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            layoutManager.setOrientation(RecyclerView.VERTICAL);
            binding.statusRecyclerView.setLayoutManager(layoutManager);
            KmConversationStatusListAdapter adapter = new KmConversationStatusListAdapter(getStatusList(), this);
            binding.statusRecyclerView.setAdapter(adapter);
        }
        return view;
    }

    public static String getFragTag() {
        return TAG;
    }

    public List<KmResolve> getStatusList() {
        List<KmResolve> statusList = new ArrayList<>();
        KmResolve assigneeItem = new KmResolve();
        assigneeItem.setStatusName(getString(R.string.km_assign_to_message));
        assigneeItem.setIconId(R.drawable.ic_assignee);
        assigneeItem.setStatusTextStyleBold(true);
        assigneeItem.setExtensionText(getAssigneeName());
        assigneeItem.setColorResId(R.color.black);
        assigneeItem.setIconTintColorId(R.color.km_assignee_icon_tint_color);
        statusList.add(assigneeItem);
        statusList.add(new KmResolve(3, KmConversationStatus.MARK_AS_SPAM, status == 3));
        return statusList;
    }

    @Override
    public void onItemClicked(View view, KmResolve data) {
        if (Utils.getString(getContext(), R.string.km_assign_to_message).equals(data.getStatusName())) {
            openFragment();
        } else {
            KmConversationStatus.updateConversationStatus(data, channel);
            dismissFragment();
        }
    }

    public void dismissFragment() {
        if (getFragmentManager() != null) {
            getFragmentManager().popBackStackImmediate();
        }
    }

    private String getAssigneeName() {
        if (channel != null) {
            Contact contact = new AppContactService(getContext()).getContactById(channel.getConversationAssignee());
            if (contact != null) {
                return contact.getDisplayName();
            }
        }
        return null;
    }

    public void openFragment() {
        if (getActivity() != null) {
            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentManager != null && fragmentManager.findFragmentByTag(KmAssigneeListFragment.getFragTag()) == null) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.idFrameLayoutFeedbackContainer, KmAssigneeListFragment.newInstance(), KmAssigneeListFragment.getFragTag());
                fragmentTransaction.addToBackStack(KmAssigneeListFragment.getFragTag());
                fragmentTransaction.commitAllowingStateLoss();
            }
        }
    }
}
