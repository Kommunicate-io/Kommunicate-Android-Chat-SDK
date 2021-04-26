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

public class ApplozicMultipartUtility {
    private static final String LINE_FEED = "\r\n";
    final String TAG = "AlMultipartUtility";
    private final String boundary;
    private HttpURLConnection httpConn;
    private OutputStream outputStream;
    private PrintWriter writer;

    public ApplozicMultipartUtility(String requestURL, String charset, Context context)
            throws IOException {

        boundary = "--" + System.currentTimeMillis() + "--";

        URL url = new URL(requestURL);
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);
        httpConn.setRequestProperty("Content-Type",
                "multipart/form-data; boundary=" + boundary);
        HttpRequestUtils httpRequestUtils = new HttpRequestUtils(context);
        httpRequestUtils.addGlobalHeaders(httpConn, null);
        outputStream = httpConn.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, charset),
                true);
    }


    public void addFilePart(String fieldName, File uploadFile, Handler handler, String oldMessageKey)
            throws IOException, InterruptedException {
        String fileName = uploadFile.getName();
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append(
                "Content-Disposition: form-data; name=\"" + fieldName
                        + "\"; filename=\"" + fileName + "\"")
                .append(LINE_FEED);
        writer.append(
                "Content-Type: "
                        + URLConnection.guessContentTypeFromName(fileName))
                .append(LINE_FEED);
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
                throw new IOException("Server exception with status code: " + status);
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
