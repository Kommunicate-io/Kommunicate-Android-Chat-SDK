package io.kommunicate;

import com.applozic.mobicomkit.feed.ChannelFeedApiResponse;

/**
 * Created by ashish on 08/03/18.
 */

public class KmConversationResponse extends ChannelFeedApiResponse {
    private String code;
    private Object data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public class KmConversationResponseData{
        private long id;
        private Integer groupId;
        private String participentUserId;
        private String status;
        private String agentId;
        private String createdBy;
        private String updated_at;
        private String created_at;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public Integer getGroupId() {
            return groupId;
        }

        public void setGroupId(Integer groupId) {
            this.groupId = groupId;
        }

        public String getParticipentUserId() {
            return participentUserId;
        }

        public void setParticipentUserId(String participentUserId) {
            this.participentUserId = participentUserId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getAgentId() {
            return agentId;
        }

        public void setAgentId(String agentId) {
            this.agentId = agentId;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
        }

        public String getUpdated_at() {
            return updated_at;
        }

        public void setUpdated_at(String updated_at) {
            this.updated_at = updated_at;
        }

        public String getCreated_at() {
            return created_at;
        }

        public void setCreated_at(String created_at) {
            this.created_at = created_at;
        }
    }
}
