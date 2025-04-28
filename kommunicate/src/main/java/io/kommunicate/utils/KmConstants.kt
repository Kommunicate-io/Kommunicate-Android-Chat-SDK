package io.kommunicate.utils

import annotations.CleanUpRequired

object KmConstants {
    const val PRECHAT_RESULT_CODE: Int = 100
    const val PRECHAT_RESULT_FAILURE: Int = 111
    const val PRECHAT_RESULT_RECEIVER: String = "kmPrechatReceiver"
    const val FINISH_ACTIVITY_RECEIVER: String = "kmFinishActivityReceiver"
    const val PRECHAT_RETURN_DATA_MAP: String = "kmPrechatReturnData"
    const val TAKE_ORDER: String = "takeOrder"
    const val USER_ID: String = "userId"
    const val GROUP_ID: String = "groupId"
    @CleanUpRequired(
        reason = "Redundant. Already present in MobiComQuickConversationFragment"
    )
    const val START_NEW_CHAT: String = "startNewChat"
    const val LOGOUT_CALL: String = "logoutCall"
    @CleanUpRequired(
        reason = "Not used anywhere"
    )
    const val PRECHAT_LOGIN_CALL: String = "prechatLogin"
    const val KM_USER_DATA: String = "kmUserData"
    const val KM_PREFILLED_MESSAGE: String = "kmPreFilledMessage"
    const val CONVERSATION_ASSIGNEE: String = "CONVERSATION_ASSIGNEE"
    const val KM_CONVERSATION_TITLE: String = "KM_CONVERSATION_TITLE"
    const val KM_HELPCENTER_URL: String = "KM_HELPCENTER_URL"
    const val PRECHAT_ACTIVITY_NAME: String = "io.kommunicate.ui.kommunicate.activities.LeadCollectionActivity"
    const val CONVERSATION_ACTIVITY_NAME: String = "io.kommunicate.ui.conversation.activity.ConversationActivity"
    const val STATUS_AWAY: Int = 2
    const val STATUS_ONLINE: Int = 3
    const val STATUS_OFFLINE: Int = 0
    const val STATUS_CONNECTED: Int = 1
    const val MESSAGE_CLUBBING_TIME_FRAME: Long = 300000L
    const val NOTIFICATION_TONE: String = "io.kommunicate.devkit.notification.tone"
    const val CLOSE_CONVERSATION_SCREEN: String = "CLOSE_CONVERSATION_SCREEN"
    const val AWS_ENCRYPTED: String = "AWS-ENCRYPTED-"
    const val KM_USER_LOCALE: String = "kmUserLocale"
    @CleanUpRequired(
        reason = "Not used anywhere. Migrated to $KM_USER_LOCALE"
    )
    const val KM_USER_LANGUAGE_CODE: String = "kmUserLanguageCode"
    const val HIDDEN: String = "hidden"
    const val PSEUDONAME: String = "pseudoName"
    const val KM_PSEUDO_USER: String = "KM_PSEUDO_USER"
    const val KM_SUMMARY: String = "KM_SUMMARY"
}