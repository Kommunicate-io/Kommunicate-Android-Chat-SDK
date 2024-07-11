package io.kommunicate.utils;



public class KMAgentStatusHelper {

    public static enum KMAgentStatus {
        ONLINE("Online"),
        OFFLINE("Offline"),
        AWAY("Away"),
        DefaultStatus("Default");

        private final String status;

        private KMAgentStatus(String c) {
            status = c;
        }

        public String getStatus() {
            return status;
        }
    }

    public interface DynamicAgentStatusChangeListener {
        void onAgentStatusChange(String assigneeId, KMAgentStatus status);
    }

    public static String assigneeID = "" ;
    public static  KMAgentStatus status = KMAgentStatus.DefaultStatus;
    public static DynamicAgentStatusChangeListener listener;

    public static void updateAssigneeStatus(String assigneeId, KMAgentStatus agentStatus) {
        assigneeID = assigneeId;
        status = agentStatus;
        if (listener != null) listener.onAgentStatusChange(assigneeId,status);
    }

    public static void setAgentStatusLister(DynamicAgentStatusChangeListener dynamicAgentStatusChangeListener) {
        listener = dynamicAgentStatusChangeListener;
    }
}
