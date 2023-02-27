package io.kommunicate.data.api.attachment.urlservice;

import android.content.Context;
import android.text.TextUtils;

import java.io.IOException;
import java.net.HttpURLConnection;

import io.kommunicate.data.api.HttpRequestUtils;
import io.kommunicate.data.api.MobiComKitClientService;
import io.kommunicate.data.api.attachment.FileClientService;
import io.kommunicate.data.conversation.Message;

public class S3URLService implements URLService {

    private static final String GET_SIGNED_URL = "/rest/ws/file/url?key=";
    private MobiComKitClientService mobiComKitClientService;
    private HttpRequestUtils httpRequestUtils;


    public S3URLService(Context context) {
        mobiComKitClientService = new MobiComKitClientService(context);
        httpRequestUtils = new HttpRequestUtils(context);
    }

    @Override
    public HttpURLConnection getAttachmentConnection(Message message) throws IOException {

        String response = httpRequestUtils.getResponse(mobiComKitClientService.getBaseUrl() + GET_SIGNED_URL + message.getFileMetas().getBlobKeyString(), "application/json", "application/json", true);
        if (TextUtils.isEmpty(response)) {
            return null;
        } else {
            return mobiComKitClientService.openHttpConnection(response);
        }
    }

    @Override
    public String getThumbnailURL(Message message) throws IOException {
        return httpRequestUtils.getResponse(mobiComKitClientService.getBaseUrl() + GET_SIGNED_URL + message.getFileMetas().getThumbnailBlobKey(), "application/json", "application/json", true);

    }

    @Override
    public String getFileUploadUrl() {
        return mobiComKitClientService.getBaseUrl() + FileClientService.S3_SIGNED_URL_END_POINT + "?" + FileClientService.S3_SIGNED_URL_PARAM + "=" + true;
    }

    @Override
    public String getImageUrl(Message message) {
        return message.getFileMetas().getBlobKeyString();
    }
}