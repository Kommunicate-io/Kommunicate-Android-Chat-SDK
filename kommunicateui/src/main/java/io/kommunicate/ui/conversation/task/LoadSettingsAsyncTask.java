package io.kommunicate.ui.conversation.task;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import java.lang.ref.WeakReference;

import io.kommunicate.commons.file.FileUtils;
import io.kommunicate.commons.json.GsonUtils;
import io.kommunicate.ui.CustomizationSettings;

public class LoadSettingsAsyncTask extends AsyncTask<Void, Void, CustomizationSettings> {

    private WeakReference<Context> context;
    private TaskListener taskListener;

    public interface TaskListener {
        void onPreExecute();
        void onPostExecute(CustomizationSettings customizationSettings);
    }

    public LoadSettingsAsyncTask(Context context, TaskListener taskListener) {
        this.context = new WeakReference<>(context);
        this.taskListener = taskListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (taskListener != null) {
            taskListener.onPreExecute();
        }
    }

    @Override
    protected CustomizationSettings doInBackground(Void... params) {
        Context taskContext = context.get();
        if (taskContext == null) {
            return new CustomizationSettings();
        }
        String jsonString = FileUtils.loadSettingsJsonFile(taskContext);
        if (!TextUtils.isEmpty(jsonString)) {
            CustomizationSettings customizationSettings = (CustomizationSettings) GsonUtils.getObjectFromJson(jsonString, CustomizationSettings.class);
            if (customizationSettings != null) {
                return customizationSettings;
            }
        }
        return new CustomizationSettings();
    }

    @Override
    protected void onPostExecute(CustomizationSettings customizationSettings) {
        super.onPostExecute(customizationSettings);
        if (taskListener != null) {
            taskListener.onPostExecute(customizationSettings);
        }
    }
}
