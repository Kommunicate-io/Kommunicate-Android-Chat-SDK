package io.kommunicate.models.feed.sync;

import java.util.List;

import io.kommunicate.data.account.user.UserDetail;
import io.kommunicate.data.json.JsonMarker;

/**
 * Created by sunil on 19/12/15.
 */
public class SyncUserDetailsResponse extends JsonMarker {

    private String status;
    private String generatedAt;
    private List<UserDetail> response;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }

    public List<UserDetail> getResponse() {
        return response;
    }

    public void setResponse(List<UserDetail> response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return "SyncUserDetailsResponse{" +
                "status='" + status + '\'' +
                ", generatedAt='" + generatedAt + '\'' +
                ", response=" + response +
                '}';
    }
}
