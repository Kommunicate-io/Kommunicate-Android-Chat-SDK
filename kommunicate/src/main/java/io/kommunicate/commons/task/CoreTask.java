package io.kommunicate.commons.task;

import annotations.CleanUpRequired;

/**
 * helper class to to execute async background tasks
 * for dependency injection
 *
 * @author shubham tewari
 */
@Deprecated
@CleanUpRequired(reason = "Migrated to coroutines")
public class CoreTask {
    public static void execute(BaseAsyncTask<?, ?> baseAsyncTask) {
        baseAsyncTask.execute();
    }
}
