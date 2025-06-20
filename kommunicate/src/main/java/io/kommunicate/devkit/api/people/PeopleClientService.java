package io.kommunicate.devkit.api.people;

import android.content.Context;

import io.kommunicate.devkit.api.HttpRequestUtils;
import io.kommunicate.devkit.api.MobiComKitClientService;

import io.kommunicate.commons.json.GsonUtils;

/**
 * Created by devashish on 27/12/14.
 */
public class PeopleClientService extends MobiComKitClientService {
    public static final String GOOGLE_CONTACT_URL = "/rest/ws/user/session/contact/google/list";
    public static final String PLATFORM_CONTACT_URL = "/rest/ws/user/session/contact/google/list";
    private HttpRequestUtils httpRequestUtils;
    private static final String appli_path = "application/json";


    public PeopleClientService(Context context) {
        super(context);
        this.httpRequestUtils = new HttpRequestUtils(context);
    }

    public String getGoogleContactUrl() {
        return getBaseUrl() + GOOGLE_CONTACT_URL;
    }

    public String getPlatformContactUrl() {
        return getBaseUrl() + PLATFORM_CONTACT_URL;
    }


    public String getGoogleContacts(int page) {
        return httpRequestUtils.getResponse(getGoogleContactUrl() + "?page=" + page, appli_path, appli_path);
    }

    public String getContactsInCurrentPlatform() {
        return httpRequestUtils.getResponse(getPlatformContactUrl() + "?mtexter=true", appli_path, appli_path);
    }

    public void addContacts(String url, ContactList contactList, boolean completed) throws Exception {
        String requestString = GsonUtils
                .getJsonWithExposeFromObject(contactList, ContactList.class);
        if (completed) {
            url = url + "?completed=true";
        }
        httpRequestUtils.postData(url, appli_path, null, requestString);
    }
}
