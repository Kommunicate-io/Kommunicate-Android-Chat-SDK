package kommunicate.io.sample.data

data class RegisterUserResponse(
    val generatedAt: Long,
    val response: Response,
    val status: String
) {
    data class Response(
        val devices: List<Any>,
        val groups: List<Any>,
        val lastFetchIndex: Int,
        val lastFetchTime: Long,
        val lastSeenFetchTime: Double,
        val totalUnreadCount: Int,
        val users: List<User>
    ) {
        data class User(
            val active: Boolean,
            val connected: Boolean,
            val connectedClientCount: Int,
            val connectedLastSeenTime: Double,
            val createdAtTime: Long,
            val deactivated: Boolean,
            val displayName: String,
            val email: String,
            val id: String,
            val lastLoggedInAtTime: Long,
            val lastSeenAtTime: Long,
            val messagePxy: MessagePxy,
            val metadata: Metadata,
            val phoneNumber: String,
            val roleKey: String,
            val roleType: Int,
            val status: Int,
            val unreadCount: Int,
            val userId: String,
            val userName: String,
            val userTypeId: Int
        ) {
            data class MessagePxy(
                val clientGroupId: String,
                val contactIds: String,
                val contentType: Int,
                val createdAtTime: Long,
                val delivered: Boolean,
                val deviceKey: String,
                val fileMeta: FileMeta,
                val fileMetaKey: String,
                val groupId: String,
                val key: String,
                val message: String,
                val metadata: Metadata,
                val pairedMessageKey: String,
                val read: Boolean,
                val sent: Boolean,
                val source: Int,
                val status: Int,
                val to: String,
                val type: Int,
                val userKey: String
            ) {
                data class FileMeta(
                    val blobKey: String,
                    val contentType: String,
                    val key: String,
                    val name: String,
                    val size: Int,
                    val thumbnailUrl: String,
                    val url: String
                )

                data class Metadata(
                    val KM_CHAT_CONTEXT: String,
                    val WELCOME_EVENT: String,
                    val contentType: String,
                    val feedback: String,
                    val payload: String,
                    val skipBot: String,
                    val templateId: String
                )
            }

            data class Metadata(
                val KM_PSEUDO_USER: String,
                val KM_SOURCE: String
            )
        }
    }
}