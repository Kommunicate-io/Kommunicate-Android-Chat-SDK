package io.kommunicate.data.api.attachment.urlservice;

import java.io.IOException;
import java.net.HttpURLConnection;

import io.kommunicate.data.conversation.Message;

public interface URLService {

    HttpURLConnection getAttachmentConnection(Message message) throws IOException;

    String getThumbnailURL(Message message) throws IOException;

    String getFileUploadUrl();

    String getImageUrl(Message message);
}
