package io.kommunicate.users;

import android.content.Context;

import com.applozic.mobicomkit.feed.ErrorResponseFeed;

import java.util.ArrayList;
import java.util.List;

import io.kommunicate.Kommunicate;
import io.kommunicate.callbacks.KMGetContactsHandler;

public class KmAssigneeListHelper {
    private static List<KmContact> agentList;
    private static List<KmContact> botList;

    public static List<KmContact> getAgentList() {
        return agentList;
    }

    public static List<KmContact> getBotList() {
        return botList;
    }

    public static boolean isAgentListEmpty() {
        return agentList == null || agentList.isEmpty();
    }

    public static boolean isBotListEmpty() {
        return botList == null || botList.isEmpty();
    }

    public static void addAgents(List<KmContact> agents) {
        if (agentList == null) {
            agentList = new ArrayList<>();
        }
        agentList.addAll(agents);
    }

    public static void addBots(List<KmContact> bots) {
        if (botList == null) {
            botList = new ArrayList<>();
        }
        botList.addAll(bots);
    }

    public static void fetchUserList(Context context) {
        Kommunicate.getAgentsList(context, 0, 100, 1, new KMGetContactsHandler() {
            @Override
            public void onSuccess(List<KmContact> contactList) {
                addAgents(contactList);
            }

            @Override
            public void onFailure(List<ErrorResponseFeed> errorResponseFeeds, Exception exception) {

            }
        });

        Kommunicate.getBotList(context, 0, 100, 1, new KMGetContactsHandler() {
            @Override
            public void onSuccess(List<KmContact> contactList) {
                addBots(contactList);
            }

            @Override
            public void onFailure(List<ErrorResponseFeed> errorResponseFeeds, Exception exception) {
            }
        });
    }
}
