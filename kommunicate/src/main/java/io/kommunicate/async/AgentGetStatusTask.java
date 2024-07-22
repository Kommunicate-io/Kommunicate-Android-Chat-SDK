package io.kommunicate.async;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.json.JsonMarker;
import com.google.gson.reflect.TypeToken;

import java.lang.ref.WeakReference;

import io.kommunicate.models.AgentAPIResponse;
import io.kommunicate.services.KmUserClientService;

/**
 * this will return the user details for the given userId and application key and then get the status from it
 * you can modify it to get user(agent) details if required
 */
public class AgentGetStatusTask extends AsyncTask<Void, Void, String> {
    private WeakReference<Context> contextWeakReference;
    private String userId;
    private KmAgentGetStatusHandler kmAgentGetStatusHandler;

    public AgentGetStatusTask(Context context, String userId, KmAgentGetStatusHandler kmAgentGetStatusHandler) {
        contextWeakReference = new WeakReference<>(context);
        this.userId = userId;
        this.kmAgentGetStatusHandler = kmAgentGetStatusHandler;
    }

    @Override
    protected String doInBackground(Void... voids) {
        KmUserClientService kmUserClientService = new KmUserClientService(contextWeakReference.get());
        return kmUserClientService.getUserDetails(userId, MobiComKitClientService.getApplicationKey(contextWeakReference.get()));
    }

    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);
        if (!TextUtils.isEmpty(response)) {
            try {
                AgentAPIResponse<AgentDetail> agentAPIResponse = (AgentAPIResponse<AgentDetail>) GsonUtils.getObjectFromJson(response, new TypeToken<AgentAPIResponse<AgentDetail>>() {}.getType());
                if (agentAPIResponse != null && agentAPIResponse.getResponse() != null && !agentAPIResponse.getResponse().isEmpty()) {
                    kmAgentGetStatusHandler.onFinished(agentAPIResponse.getResponse().get(0).status == 1);
                } else {
                    kmAgentGetStatusHandler.onError("Response object is null, but the response string isn't empty or null.");
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                kmAgentGetStatusHandler.onError(exception.getMessage());
            }
        } else {
            kmAgentGetStatusHandler.onError("The response string is null.");
        }
    }

    public interface KmAgentGetStatusHandler {
        void onFinished(boolean status);
        void onError(String error);
    }

    public static class AgentDetail extends JsonMarker {
        String userName;
        int status;
    }
}
