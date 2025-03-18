package io.kommunicate.commons.task;

import io.kommunicate.commons.task.executor.ExecutorAsyncTask;

/**
 * this class has been created to help in refactoring in case of implementation change for background async tasks
 * refer to {@link BaseAsyncTask} for more information
 *
 * @author shubham tewari
 */
public class AlAsyncTask<Progress, Result> extends ExecutorAsyncTask<Progress, Result> { }
