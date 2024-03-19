package com.applozic.mobicomkit.uiwidgets.async;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;

import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.uiwidgets.kommunicate.callbacks.PrePostUIMethods;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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
            uri = compressImage(uri,context,file.getName());
        }
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

    private Uri compressImage(Uri uri, Context context, String fileName) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            Bitmap originalBitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            originalBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);

            File tempFile = File.createTempFile(fileName,null, context.getCacheDir());
            tempFile.deleteOnExit();

            FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
            fileOutputStream.write(outputStream.toByteArray());
            fileOutputStream.flush();
            fileOutputStream.close();

            return Uri.fromFile(tempFile);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
