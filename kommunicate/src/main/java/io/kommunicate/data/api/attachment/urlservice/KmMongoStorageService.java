package io.kommunicate.data.api.attachment.urlservice;

import android.content.Context;

import java.io.IOException;
import java.net.HttpURLConnection;

import io.kommunicate.data.api.MobiComKitClientService;
import io.kommunicate.data.conversation.Message;

public class KmMongoStorageService implements URLService {

    private static final String UPLOAD_URL = "/files/v2/upload";
    private static final String DOWNLOAD_URL = "/files/get/";
    private MobiComKitClientService mobiComKitClientService;


    public KmMongoStorageService(Context context) {
        mobiComKitClientService = new MobiComKitClientService(context);
    }

    @Override
    public HttpURLConnection getAttachmentConnection(Message message) throws IOException {

        return mobiComKitClientService.openHttpConnection(mobiComKitClientService.getFileBaseUrl()
                + DOWNLOAD_URL
                + message.getFileMetas().getBlobKeyString());

    }

    @Override
    public String getThumbnailURL(Message message) throws IOException {
        return message.getFileMetas().getThumbnailUrl();
    }

    @Override
    public String getFileUploadUrl() {
        return mobiComKitClientService.getFileBaseUrl() + UPLOAD_URL;
    }

    @Override
    public String getImageUrl(Message message) {
        return message.getFileMetas().getBlobKeyString();
    }
}
