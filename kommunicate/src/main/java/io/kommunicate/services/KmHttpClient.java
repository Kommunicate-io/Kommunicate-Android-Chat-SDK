package io.kommunicate.services;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.encryption.EncryptionUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import javax.crypto.IllegalBlockSizeException;

public class KmHttpClient {

    private static final String TAG = "KmHttpClient";
    private Context context;
    private static final String ACCEPT = "Accept";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String POST = "POST";
    private static final String GET = "GET";

    public KmHttpClient(Context context) {
        this.context = ApplozicService.getContext(context);
    }

    public String postData(String urlString, String contentType, String accept, String data) throws Exception {
        Utils.printLog(context, TAG, "Calling url: " + urlString);
        HttpURLConnection connection;
        URL url;
        try {
            if (!TextUtils.isEmpty(MobiComUserPreference.getInstance(context).getEncryptionKey())) {
                data = EncryptionUtils.encrypt(MobiComUserPreference.getInstance(context).getEncryptionKey(), data, MobiComUserPreference.getInstance(context).getEncryptionIV());
            }
            url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(POST);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            if (!TextUtils.isEmpty(contentType)) {
                connection.setRequestProperty(CONTENT_TYPE, contentType);
            }
            if (!TextUtils.isEmpty(accept)) {
                connection.setRequestProperty(ACCEPT, accept);
            }
            connection.connect();

            if (data != null) {
                byte[] dataBytes = data.getBytes("UTF-8");
                DataOutputStream os = new DataOutputStream(connection.getOutputStream());
                os.write(dataBytes);
                os.flush();
                os.close();
            }
            BufferedReader br = null;
            if (connection.getResponseCode() == 200 || connection.getResponseCode() == 201) {
                InputStream inputStream = connection.getInputStream();
                br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            }
            StringBuilder sb = new StringBuilder();
            try {
                String line;
                if (br != null) {
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    br.close();
                }
            }
            Utils.printLog(context, TAG, "Response : " + sb.toString());
            if (!TextUtils.isEmpty(sb.toString())) {
                if (!TextUtils.isEmpty(MobiComUserPreference.getInstance(context).getEncryptionKey())) {
                    return EncryptionUtils.decrypt(MobiComUserPreference.getInstance(context).getEncryptionKey(), sb.toString(), MobiComUserPreference.getInstance(context).getEncryptionIV());
                }
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getResponseWithException(String urlString, String contentType, String accept, Map<String, String> headers) throws Throwable {
        Utils.printLog(context, TAG, "Calling url **[GET]** : " + urlString);

        HttpURLConnection connection = null;
        MobiComUserPreference userPreference = MobiComUserPreference.getInstance(context);
        URL url;

        try {
            url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod(GET);
            connection.setUseCaches(false);
            connection.setDoInput(true);

            if (!TextUtils.isEmpty(contentType)) {
                connection.setRequestProperty(CONTENT_TYPE, contentType);
            }
            if (!TextUtils.isEmpty(accept)) {
                connection.setRequestProperty(ACCEPT, accept);
            }
            if(headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    connection.setRequestProperty(header.getKey(), header.getValue());
                }
            }
            connection.connect();

            BufferedReader br = null;
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            } else {
                Utils.printLog(context, TAG, "\n\nResponse code for url: " + urlString + "\n** Code ** : " + connection.getResponseCode() + "\n\n");
            }

            StringBuilder sb = new StringBuilder();
            try {
                String line;
                if (br != null) {
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            } finally {
                if (br != null) {
                    br.close();
                }
            }

            Utils.printLog(context, TAG, "\n\nGET Response for url: " + urlString + "\n** Response **: " + sb.toString() + "\n\n");
            return sb.toString();
        } catch (ConnectException e) {
            Utils.printLog(context, TAG, "failed to connect Internet is not working");
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } catch (Throwable e) {
            throw e;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
