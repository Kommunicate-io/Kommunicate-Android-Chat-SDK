package io.kommunicate.utils

object KMAgentStatusHelper {
    @JvmField
    var assigneeID: String = ""
    @JvmField
    var status: KMAgentStatus = KMAgentStatus.DefaultStatus
    var listener: DynamicAgentStatusChangeListener? = null

    @JvmStatic
    fun updateAssigneeStatus(assigneeId: String, agentStatus: KMAgentStatus) {
        assigneeID = assigneeId
        status = agentStatus
        listener?.onAgentStatusChange(assigneeId, status)
    }

    @JvmStatic
    fun setAgentStatusLister(dynamicAgentStatusChangeListener: DynamicAgentStatusChangeListener?) {
        listener = dynamicAgentStatusChangeListener
    }

    enum class KMAgentStatus(val status: String) {
        ONLINE("Online"),
        OFFLINE("Offline"),
        AWAY("Away"),
        DefaultStatus("Default")
    }

    interface DynamicAgentStatusChangeListener {
        fun onAgentStatusChange(assigneeId: String?, status: KMAgentStatus?)
    }
}
