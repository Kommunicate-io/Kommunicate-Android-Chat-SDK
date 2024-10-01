package com.applozic.mobicomkit.api.attachment;

import android.content.Context;
import android.os.Handler;

import com.applozic.mobicomkit.api.HttpRequestUtils;
import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.conversation.MobiComConversationService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import io.kommunicate.network.SSLPinningConfig;

public class ApplozicMultipartUtility {
    private static final String LINE_FEED = "\r\n";
    private static final String AWS_ENCRYPTED = "AWS-ENCRYPTED-";
    final String TAG = "AlMultipartUtility";
    private final String boundary;
    private HttpsURLConnection httpConn;
    private OutputStream outputStream;
    private PrintWriter writer;
    private boolean isUploadOveridden;
    private static final String content_type = "Content-Type";
    private static final String multipart_form = "multipart/form-data; boundary=";
    private static final String server_exception = "Server exception with status code: ";
    private static final String content_disposition = "Content-Disposition: form-data; name=\"";


    public ApplozicMultipartUtility(String requestURL, String charset, Context context)
            throws IOException {

        boundary = "--" + System.currentTimeMillis() + "--";

        URL url = new URL(requestURL);
        httpConn = (HttpsURLConnection) url.openConnection();
        httpConn.setSSLSocketFactory(SSLPinningConfig.createPinnedSSLSocketFactory());
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);
        httpConn.setRequestProperty(content_type, multipart_form + boundary);
        HttpRequestUtils httpRequestUtils = new HttpRequestUtils(context);
        httpRequestUtils.addGlobalHeaders(httpConn, null);
        outputStream = httpConn.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
    }

    public ApplozicMultipartUtility(String requestURL, HashMap<String, String> headers, Context context) throws IOException {
        boundary = "--" + System.currentTimeMillis() + "--";
        isUploadOveridden = true;
        URL url = new URL(requestURL);
        httpConn = (HttpsURLConnection) url.openConnection();
        httpConn.setSSLSocketFactory(SSLPinningConfig.createPinnedSSLSocketFactory());
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);
        httpConn.setRequestProperty(content_type,multipart_form + boundary);
        if(headers != null && !headers.isEmpty()) {
            for(Map.Entry<String, String> entry : headers.entrySet()) {
                httpConn.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        outputStream = httpConn.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"),
                true);
    }
    public void addFormField(String name, String contentType, String value) {
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append(content_disposition + name + "\";").append(LINE_FEED);
        writer.append("Content-Type: ").append(contentType).append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
    }

    public void addFilePart(String fieldName, File uploadFile, Handler handler, String oldMessageKey)
            throws IOException, InterruptedException {
        String fileName = uploadFile.getName();
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append(content_disposition + fieldName + "\"; filename=\"" + (isUploadOveridden? "" : AWS_ENCRYPTED) + fileName + "\"").append(LINE_FEED);
        writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName)).append(LINE_FEED);
        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

        FileInputStream inputStream = new FileInputStream(uploadFile);
        byte[] buffer = new byte[4096];
        long bytesRead = -1;
        long totalRead = 0;
        long previousPercent = 0;
        long totalSize = uploadFile.length();
        if (handler != null) {
            android.os.Message msg = handler.obtainMessage();
            msg.what = MobiComConversationService.UPLOAD_STARTED;
            msg.getData().putString(MobiComKitConstants.OLD_MESSAGE_KEY_INTENT_EXTRA, oldMessageKey);
            msg.getData().putString("error", null);
            msg.sendToTarget();
            //handler.onUploadStarted(null);
        }
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            try {
                outputStream.write(buffer, 0, (int) bytesRead);
                totalRead += bytesRead;
                int percentage = (int) ((totalRead / (float) totalSize) * 100);

                if (percentage != previousPercent) {
                    if (handler != null) {
                        android.os.Message msg = handler.obtainMessage();
                        msg.what = MobiComConversationService.UPLOAD_PROGRESS;
                        msg.getData().putString(MobiComKitConstants.OLD_MESSAGE_KEY_INTENT_EXTRA, oldMessageKey);
                        msg.arg1 = percentage;
                        msg.sendToTarget();
                        //handler.onProgressUpdate(percentage, null);
                    }
                    previousPercent = percentage;
                }
            } catch (Exception e) {
                if (handler != null) {
                    android.os.Message msg = handler.obtainMessage();
                    msg.what = MobiComConversationService.UPLOAD_COMPLETED;
                    msg.getData().putString(MobiComKitConstants.OLD_MESSAGE_KEY_INTENT_EXTRA, oldMessageKey);
                    msg.getData().putString("error", e.getMessage());
                    msg.sendToTarget();
                    //handler.onCompleted(new ApplozicException(e.getMessage()));
                }
            }
        }
        outputStream.flush();
        inputStream.close();

        writer.append(LINE_FEED);
        writer.flush();
    }


    public String getResponse() throws IOException {
        StringBuilder sb = new StringBuilder();
        writer.append(LINE_FEED).flush();
        writer.append("--" + boundary + "--").append(LINE_FEED);
        writer.close();
        BufferedReader reader = null;
        int status = httpConn.getResponseCode();
        try {
            if (status == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } else {
                throw new IOException(server_exception + status);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
                httpConn.disconnect();
            }
        }
        return sb.toString();
    }

}
