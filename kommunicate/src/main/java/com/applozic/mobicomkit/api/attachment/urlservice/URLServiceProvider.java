package com.applozic.mobicomkit.api.attachment.urlservice;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicommons.ApplozicService;

import java.io.IOException;
import java.net.HttpURLConnection;

public class URLServiceProvider {

    private Context context;
    private URLService urlService;
    private URLService S3UrlService;
    private MobiComKitClientService mobiComKitClientService;

    public URLServiceProvider(Context context) {
        this.context = ApplozicService.getContext(context);
        mobiComKitClientService = new MobiComKitClientService(context);
    }

    private URLService getUrlService(Context context) {

        if (urlService != null) {
            return urlService;
        }

        ApplozicClient appClient = ApplozicClient.getInstance(context);

        if (appClient.isS3StorageServiceEnabled()) {
            urlService = new S3URLService(context);
        } else if (appClient.isGoogleCloudServiceEnabled()) {
            urlService = new GoogleCloudURLService(context);
        } else if (appClient.isStorageServiceEnabled()) {
            urlService = new ApplozicMongoStorageService(context);
        } else {
            urlService = new DefaultURLService(context);
        }

        return urlService;
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
            if(message.isAttachmentEncrypted()) {
                connection = getS3UrlService(context).getAttachmentConnection(message);
            }
            else {
                connection = getUrlService(context).getAttachmentConnection(message);
            }
        } catch (Exception e) {
            throw new IOException("Error connecting");
        }
        return connection;
    }

    public String getThumbnailURL(Message message) throws IOException {
        try {
            if(message.isAttachmentEncrypted()) {
                return getS3UrlService(context).getThumbnailURL(message);
            }
            else {
                return getUrlService(context).getThumbnailURL(message);
            }
        } catch (Exception e) {
            throw new IOException("Error connecting");
        }
    }

    public String getFileUploadUrl() {
        return getS3UrlService(context).getFileUploadUrl();
    }

    public String getImageURL(Message message) {
        return getUrlService(context).getImageUrl(message);
    }

}
