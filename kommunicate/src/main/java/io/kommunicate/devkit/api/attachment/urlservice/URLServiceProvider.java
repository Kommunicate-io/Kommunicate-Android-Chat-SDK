package io.kommunicate.devkit.api.attachment.urlservice;

import android.content.Context;

import io.kommunicate.devkit.api.MobiComKitClientService;
import io.kommunicate.devkit.api.conversation.Message;
import io.kommunicate.commons.AppContextService;

import java.io.IOException;
import java.net.HttpURLConnection;

public class URLServiceProvider {

    private Context context;
    private URLService defaultUrlService;
    private URLService S3UrlService;
    private MobiComKitClientService mobiComKitClientService;
    private static final String err_msg = "Error connecting";

    public URLServiceProvider(Context context) {
        this.context = AppContextService.getContext(context);
        mobiComKitClientService = new MobiComKitClientService(context);
    }

    private URLService getUrlService(Context context) {
        return getUrlService(context, null);
    }
    private URLService getUrlService(Context context, Message message) {
        if(message != null && message.isAttachmentEncrypted()) {
            if (S3UrlService != null) {
                return S3UrlService;
            }
            S3UrlService = new S3URLService(context);
            return S3UrlService;
        }
        if (defaultUrlService != null) {
            return defaultUrlService;
        }
        defaultUrlService = new DefaultURLService(context);
        return defaultUrlService;
    }

    private URLService getS3UrlService(Context context) {
        if (S3UrlService != null) {
            return S3UrlService;
        }
        S3UrlService = new S3URLService(context);
        return S3UrlService;
    }


    public HttpURLConnection getDownloadConnection(Message message) throws IOException {
        HttpURLConnection connection;

        try {
                connection = getUrlService(context, message).getAttachmentConnection(message);
        } catch (Exception e) {
            throw new IOException(err_msg);
        }
        return connection;
    }

    public String getThumbnailURL(Message message) throws IOException {
        try {
                return getUrlService(context, message).getThumbnailURL(message);
        } catch (Exception e) {
            throw new IOException(err_msg);
        }
    }

    public String getFileUploadUrl() {
        return getS3UrlService(context).getFileUploadUrl();
    }

    public String getImageURL(Message message) {
        return getUrlService(context).getImageUrl(message);
    }

}
