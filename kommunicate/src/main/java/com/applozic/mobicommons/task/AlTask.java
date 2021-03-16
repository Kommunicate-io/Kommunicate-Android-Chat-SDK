package com.applozic.mobicommons.task;

/**
 * helper class to to execute async background tasks
 * for dependency injection
 *
 * @author shubham tewari
 */
public class AlTask {
    public static void execute(BaseAsyncTask<?, ?> baseAsyncTask) {
        baseAsyncTask.execute();
    }
}
