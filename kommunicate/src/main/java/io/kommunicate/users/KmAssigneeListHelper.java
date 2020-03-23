package io.kommunicate.users;

import android.content.Context;
import android.util.SparseArray;

import com.applozic.mobicomkit.feed.ErrorResponseFeed;

import java.util.ArrayList;
import java.util.List;

import io.kommunicate.Kommunicate;
import io.kommunicate.callbacks.KMGetContactsHandler;

public class KmAssigneeListHelper {
    public static final Integer BOT_LIST_CODE = 0;
    public static final Integer AGENT_LIST_CODE = 1;
    private static final SparseArray<List<KmContact>> assigneeListSparseArray = new SparseArray<>();

    public static List<KmContact> getAssigneeList(Integer assigneeTypeCode) {
        return assigneeListSparseArray.get(assigneeTypeCode);
    }

    public static boolean isListEmpty(Integer assigneeTypeCode) {
        List<KmContact> assigneeList = assigneeListSparseArray.get(assigneeTypeCode);
        return assigneeList == null || assigneeList.isEmpty();
    }

    public static void addAssigneeList(Integer assigneeTypeCode, List<KmContact> assigneeList) {
        List<KmContact> existingAssigneeList = assigneeListSparseArray.get(assigneeTypeCode);
        if (existingAssigneeList == null) {
            existingAssigneeList = new ArrayList<>();
        }
        if (existingAssigneeList.isEmpty()) {
            existingAssigneeList.addAll(assigneeList);
            assigneeListSparseArray.put(assigneeTypeCode, existingAssigneeList);
        }
    }

    public static void fetchAssigneeList(Context context) {
        Kommunicate.fetchAgentList(context, 0, 100, 1, new KMGetContactsHandler() {
            @Override
            public void onSuccess(List<KmContact> contactList) {
                addAssigneeList(AGENT_LIST_CODE, contactList);
            }

            @Override
            public void onFailure(List<ErrorResponseFeed> errorResponseFeeds, Exception exception) {

            }
        });

        Kommunicate.fetchBotList(context, 0, 100, 1, new KMGetContactsHandler() {
            @Override
            public void onSuccess(List<KmContact> contactList) {
                addAssigneeList(BOT_LIST_CODE, contactList);
            }

            @Override
            public void onFailure(List<ErrorResponseFeed> errorResponseFeeds, Exception exception) {
            }
        });
    }
}
