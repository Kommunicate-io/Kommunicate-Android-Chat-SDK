package com.applozic.mobicommons.task;

/**
 * implement this interface to create a background thread running task
 * for proper working follow the comments written on each function
 * you can also refer to {@link android.os.AsyncTask}, the working(not implementation) is supposed to be similar to that
 * to create and use your own implementation, simply implement this interface and extend it in {@link AlAsyncTask}
 *
 * @author shubham tewari
 *
 * @param <Progress> type of the progress parameter passed to {@link #publishProgress(Object)} and {@link #onProgress(Object)}
 * @param <Result> type of the result returned by {@link #doInBackground()} and passed to {@link #onPostExecute(Object)}
 */
public abstract class BaseAsyncTask<Progress, Result> {
    public BaseAsyncTask() { }

    protected void onPreExecute() { } //call this in UI thread before call()
    protected Result doInBackground() throws Exception { return null; } //call this in background thread
    protected void onPostExecute(Result result) { } //call this in UI thread after call()
    protected void onProgress(Progress progress) { } //call this in the UI thread; make your own implementation for it
    protected void publishProgress(Progress progress) { } //add implementation to call onProgress in UI thread
    public abstract void execute(); //the code for execution of all the above listed functions
    protected void onCancelled() { } //this code will be run if task is cancelled
}
