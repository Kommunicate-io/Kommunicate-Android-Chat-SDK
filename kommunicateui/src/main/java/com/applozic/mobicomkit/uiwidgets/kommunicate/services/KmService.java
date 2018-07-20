package com.applozic.mobicomkit.uiwidgets.kommunicate.services;

import android.content.Context;

/**
 * Created by ashish on 03/04/18.
 */

public class KmService {

    private Context context;
    private KmClientService clientService;

    public KmService(Context context) {
        this.context = context;
        clientService = new KmClientService(context);
    }

    public String getAwayMessage(String appKey, Integer groupId) throws Exception {
        String response = clientService.getAwayMessage(appKey, groupId);
        if (response == null) {
            return null;
        }
        return response;
    }
}
