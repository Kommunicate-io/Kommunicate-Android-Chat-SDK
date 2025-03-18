package com.applozic.mobicomkit.uiwidgets.async;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;

import io.kommunicate.devkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.uiwidgets.kommunicate.callbacks.PrePostUIMethods;
import io.kommunicate.commons.file.FileUtils;

import java.io.File;

/**
 * a async task that will write the given file with the given "uri" to the "file"
 * pre post methods can be implemented accordingly
 */
public class FileTaskAsync extends AsyncTask<Void, Integer, Boolean> {
    Context context;
    FileClientService fileClientService;
    File file;
    Uri uri;
    private static final String IMAGE = "image/";
    private static final String VIDEO = "video/";

    PrePostUIMethods prePostUIMethods;
    boolean isCompressionNeeded;

    public FileTaskAsync(File file, Uri uri, Context context, PrePostUIMethods prePostUIMethods, boolean isCompressionNeeded) {
        this.context = context;
        this.file = file;
        this.uri = uri;
        this.fileClientService = new FileClientService(context);
        this.prePostUIMethods = prePostUIMethods;
        this.isCompressionNeeded = isCompressionNeeded;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        prePostUIMethods.preTaskUIMethod();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        if (isCompressionNeeded) {
            String mimeType = FileUtils.getMimeTypeByContentUriOrOther(context, uri);
            if (!TextUtils.isEmpty(mimeType) && mimeType.contains(IMAGE)) {
                uri = FileUtils.compressImage(uri, context, file.getName());
            } else if (!TextUtils.isEmpty(mimeType) && mimeType.contains(VIDEO)) {
                uri = FileUtils.compressVideo(context, uri, file);
            }
        }
        if (fileClientService != null) {
            fileClientService.writeFile(uri, file);
            FileUtils.deleteFile(uri);
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean completed) {
        super.onPostExecute(completed);
        prePostUIMethods.postTaskUIMethod(uri, completed, file);
    }
}
