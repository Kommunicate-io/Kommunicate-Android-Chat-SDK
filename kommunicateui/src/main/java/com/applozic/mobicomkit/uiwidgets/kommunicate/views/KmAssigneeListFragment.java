package com.applozic.mobicomkit.uiwidgets.kommunicate.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.applozic.mobicomkit.feed.ErrorResponseFeed;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.adapter.KmUserPagerAdapter;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.KmUserListFragment;
import com.applozic.mobicomkit.uiwidgets.databinding.KmAssigneeListLayoutBinding;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import io.kommunicate.Kommunicate;
import io.kommunicate.callbacks.KMGetContactsHandler;
import io.kommunicate.users.KmAssigneeListHelper;
import io.kommunicate.users.KmContact;

public class KmAssigneeListFragment extends Fragment implements TabLayout.OnTabSelectedListener, SearchView.OnQueryTextListener {
    private static final String TAG = "KmAssigneeListFragment";
    private String assigneeId;
    private KmAssigneeListLayoutBinding binding;
    private KmUserPagerAdapter pagerAdapter;
    private String searchText;
    private int channelId;

    public static String getFragTag() {
        return TAG;
    }

    public static KmAssigneeListFragment newInstance(String assigneeId, int channelId) {
        KmAssigneeListFragment assigneeListFragment = new KmAssigneeListFragment();
        Bundle args = new Bundle();
        args.putString(Channel.CONVERSATION_ASSIGNEE, assigneeId);
        args.putInt(ConversationUIService.GROUP_ID, channelId);
        assigneeListFragment.setArguments(args);
        return assigneeListFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            assigneeId = bundle.getString(Channel.CONVERSATION_ASSIGNEE, null);
            channelId = bundle.getInt(ConversationUIService.GROUP_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.km_assignee_list_layout, container, false);

        binding = DataBindingUtil.bind(view.findViewById(R.id.kmAssigneeListLayout));
        List<String> titleList = new ArrayList<>();
        titleList.add(getString(R.string.km_agent_list_title));
        titleList.add(getString(R.string.km_bot_list_title));

        if (binding != null) {
            binding.setAssigneeListFragment(this);
            pagerAdapter = new KmUserPagerAdapter(getFragmentManager(), titleList);
            KmUserListFragment agentsFragment = KmUserListFragment.getInstance(assigneeId, true, channelId, KmAssigneeListHelper.getAgentList());
            KmUserListFragment botsFragment = KmUserListFragment.getInstance(assigneeId, false, channelId, KmAssigneeListHelper.getBotList());
            pagerAdapter.addFragment(agentsFragment);
            pagerAdapter.addFragment(botsFragment);

            binding.kmAssigneeSearchView.setOnQueryTextListener(this);
            binding.kmAssigneeViewPager.setAdapter(pagerAdapter);
            binding.kmAssignTabLayout.setupWithViewPager(binding.kmAssigneeViewPager);
            binding.kmAssignTabLayout.addOnTabSelectedListener(this);

            if (KmAssigneeListHelper.isAgentListEmpty()) {
                Kommunicate.getAgentsList(getContext(), 0, 100, 1, new KMGetContactsHandler() {
                    @Override
                    public void onSuccess(List<KmContact> contactList) {
                        pagerAdapter.getItem(0).addUserList(contactList);
                    }

                    @Override
                    public void onFailure(List<ErrorResponseFeed> errorResponseFeeds, Exception exception) {
                        pagerAdapter.setErrorText(0);
                        Toast.makeText(getContext(), Utils.getString(getContext(), R.string.applozic_server_error) + " :" + getError(errorResponseFeeds, exception), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (KmAssigneeListHelper.isBotListEmpty()) {
                Kommunicate.getBotList(getContext(), 0, 100, 1, new KMGetContactsHandler() {
                    @Override
                    public void onSuccess(List<KmContact> contactList) {
                        pagerAdapter.getItem(1).addUserList(contactList);
                    }

                    @Override
                    public void onFailure(List<ErrorResponseFeed> errorResponseFeeds, Exception exception) {
                        pagerAdapter.setErrorText(1);
                        Toast.makeText(getContext(), Utils.getString(getContext(), R.string.applozic_server_error) + " :" + getError(errorResponseFeeds, exception), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        return view;
    }

    public void dismissFragment() {
        if (getFragmentManager() != null) {
            getFragmentManager().popBackStackImmediate();
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if (binding != null) {
            binding.kmAssigneeViewPager.setCurrentItem(tab.getPosition(), true);

            if (pagerAdapter != null) {
                pagerAdapter.setSearchText(searchText, tab.getPosition());
            }
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        this.searchText = newText;
        if (binding != null && pagerAdapter != null) {
            pagerAdapter.setSearchText(newText, binding.kmAssigneeViewPager.getCurrentItem());
        }
        return false;
    }

    public String getError(List<ErrorResponseFeed> errorResponseFeeds, Exception exception) {
        if (errorResponseFeeds != null) {
            return GsonUtils.getJsonFromObject(errorResponseFeeds, List.class);
        } else if (exception != null) {
            return exception.getLocalizedMessage();
        }
        return "";
    }
}
