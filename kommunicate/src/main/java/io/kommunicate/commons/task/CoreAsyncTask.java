package io.kommunicate.commons.task;

import annotations.CleanUpRequired;
import io.kommunicate.commons.task.executor.ExecutorAsyncTask;

/**
 * this class has been created to help in refactoring in case of implementation change for background async tasks
 * refer to {@link BaseAsyncTask} for more information
 *
 * @author shubham tewari
 */
@Deprecated
@CleanUpRequired(reason = "Migrated to coroutines")
public class CoreAsyncTask<Progress, Result> extends ExecutorAsyncTask<Progress, Result> { }
