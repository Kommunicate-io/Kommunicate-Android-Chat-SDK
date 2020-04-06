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
import java.net.HttpURLConnection;
import java.net.URL;

public class KmHttpClient {

    private static final String TAG = "KmHttpClient";
    private Context context;

    public KmHttpClient(Context context) {
        this.context = ApplozicService.getContext(context);
    }

    public String postData(String urlString, String contentType, String accept, String data) throws Exception {
        Utils.printLog(context, TAG, "Calling url: " + urlString);
        HttpURLConnection connection;
        URL url;
        try {
            if (!TextUtils.isEmpty(MobiComUserPreference.getInstance(context).getEncryptionKey())) {
                data = EncryptionUtils.encrypt(MobiComUserPreference.getInstance(context).getEncryptionKey(), data);
            }
            url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            if (!TextUtils.isEmpty(contentType)) {
                connection.setRequestProperty("Content-Type", contentType);
            }
            if (!TextUtils.isEmpty(accept)) {
                connection.setRequestProperty("Accept", accept);
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
                    return EncryptionUtils.decrypt(MobiComUserPreference.getInstance(context).getEncryptionKey(), sb.toString());
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
}
