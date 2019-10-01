package com.applozic.mobicomkit.uiwidgets.async;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.uiwidgets.kommunicate.callbacks.PrePostUIMethods;

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

    PrePostUIMethods prePostUIMethods;

    public FileTaskAsync(File file, Uri uri, Context context, PrePostUIMethods prePostUIMethods) {
        this.context = context;
        this.file = file;
        this.uri = uri;
        this.fileClientService = new FileClientService(context);
        this.prePostUIMethods = prePostUIMethods;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        prePostUIMethods.preTaskUIMethod();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        if (fileClientService != null) {
            fileClientService.writeFile(uri, file);
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean completed) {
        super.onPostExecute(completed);
        prePostUIMethods.postTaskUIMethod(completed, file);
    }
}
