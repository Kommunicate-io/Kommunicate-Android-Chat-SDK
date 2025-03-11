package dev.kommunicate.devkit.api.attachment.urlservice;

import android.content.Context;
import android.text.TextUtils;

import dev.kommunicate.devkit.api.HttpRequestUtils;
import dev.kommunicate.devkit.api.MobiComKitClientService;
import dev.kommunicate.devkit.api.conversation.Message;

import java.io.IOException;
import java.net.HttpURLConnection;

public class GoogleCloudURLService implements URLService {

    private MobiComKitClientService mobiComKitClientService;
    private HttpRequestUtils httpRequestUtils;
    private static final String GET_SIGNED_URL = "/files/url?key=";
    private static final String UPLOAD_URL = "/files/upload";


    public GoogleCloudURLService(Context context) {
        mobiComKitClientService = new MobiComKitClientService(context);
        httpRequestUtils = new HttpRequestUtils(context);
    }


    @Override
    public HttpURLConnection getAttachmentConnection(Message message) throws IOException {

        String response = httpRequestUtils.getResponse(mobiComKitClientService.getFileBaseUrl() + GET_SIGNED_URL + message.getFileMetas().getBlobKeyString(), "application/json", "application/json",true);
        if (TextUtils.isEmpty(response)) {
            return null;
        } else {
            return mobiComKitClientService.openHttpConnection(response);
        }
    }

    @Override
    public String getThumbnailURL(Message message) throws IOException {
        return httpRequestUtils.getResponse(mobiComKitClientService.getFileBaseUrl() + GET_SIGNED_URL + message.getFileMetas().getThumbnailBlobKey(), "application/json", "application/json",true);
    }


    @Override
    public String getFileUploadUrl() {
        return mobiComKitClientService.getFileBaseUrl() + UPLOAD_URL;
    }

    @Override
    public String getImageUrl(Message message) {
        return message.getFileMetas().getUrl();
    }
}
