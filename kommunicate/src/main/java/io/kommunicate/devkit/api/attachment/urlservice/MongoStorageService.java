package io.kommunicate.devkit.api.attachment.urlservice;

import android.content.Context;

import annotations.CleanUpRequired;
import io.kommunicate.devkit.api.MobiComKitClientService;
import io.kommunicate.devkit.api.conversation.Message;

import java.io.IOException;
import java.net.HttpURLConnection;

@Deprecated
@CleanUpRequired(reason = "Not used anywhere")
public class MongoStorageService implements URLService {

    private MobiComKitClientService mobiComKitClientService;

    private static final String UPLOAD_URL ="/files/v2/upload";
    private static final String DOWNLOAD_URL ="/files/get/";


    public MongoStorageService(Context context) {
        mobiComKitClientService = new MobiComKitClientService(context);
    }

    @Override
    public HttpURLConnection getAttachmentConnection(Message message) throws IOException {

        return mobiComKitClientService.openHttpConnection( mobiComKitClientService.getFileBaseUrl()
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
