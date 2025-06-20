package io.kommunicate.devkit.api.attachment.urlservice;

import android.content.Context;
import android.text.TextUtils;

import io.kommunicate.devkit.api.HttpRequestUtils;
import io.kommunicate.devkit.api.MobiComKitClientService;
import io.kommunicate.devkit.api.attachment.FileClientService;
import io.kommunicate.devkit.api.conversation.Message;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Date;

public class DefaultURLService implements URLService {

    private MobiComKitClientService mobiComKitClientService;
    private HttpRequestUtils httpRequestUtils;


    public DefaultURLService(Context context) {
        this.httpRequestUtils = new HttpRequestUtils(context);
        mobiComKitClientService = new MobiComKitClientService(context);
    }

    @Override
    public HttpURLConnection getAttachmentConnection(Message message) throws IOException {
        return !TextUtils.isEmpty(message.getFileMetas().getUrl()) ? mobiComKitClientService.openHttpConnection(message.getFileMetas().getUrl()) : mobiComKitClientService.openHttpConnection(mobiComKitClientService.getFileUrl() + message.getFileMetas().getBlobKeyString());
    }

    @Override
    public String getThumbnailURL(Message message) throws IOException {
        return message.getFileMetas().getThumbnailUrl();
    }

    @Override
    public String getFileUploadUrl() {
        return httpRequestUtils.getResponse(mobiComKitClientService.getFileBaseUrl() + FileClientService.FILE_UPLOAD_URL
                + "?data=" + new Date().getTime(), "text/plain", "text/plain", true);
    }

    @Override
    public String getImageUrl(Message message) {
        return mobiComKitClientService.getFileUrl() + message.getFileMetas().getBlobKeyString();
    }

}
