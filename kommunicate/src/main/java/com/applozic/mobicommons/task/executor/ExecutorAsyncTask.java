package com.applozic.mobicommons.task.executor;

import android.os.Binder;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.applozic.mobicommons.task.BaseAsyncTask;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * this implementation of the {@link BaseAsyncTask} uses {@link ExecutorService}, {@link Future} and {@link Handler}
 * this is very similar to the now deprecated {@link android.os.AsyncTask}, the source code was continuously referenced

 * @author shubham tewari
 */
public abstract class ExecutorAsyncTask<Progress, Result> extends BaseAsyncTask<Progress, Result> {
    private static final String TAG = "ExecutorAsyncTask";

    private final @NonNull Executor executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
    private final @NonNull Handler handler = new Handler(Looper.getMainLooper());
    FutureTask<Result> future;

    private final AtomicBoolean cancelled = new AtomicBoolean();
    private final AtomicBoolean taskInvoked = new AtomicBoolean();
    private Status status = Status.PENDING;

    public Status getStatus() {
        return status;
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    WorkerRunnable<Result> worker = new WorkerRunnable<Result>() {
        @Override
        public Result call() throws Exception {
            taskInvoked.set(true);
            Result result = null;
            try {
                result = doInBackground();
                Binder.flushPendingCommands();
            } catch (Throwable t) {
                cancelled.set(true);
                throw t;
            } finally {
                status = Status.FINISHED;
                postResult(result);
            }
            return result;
        }
    };

    @Override
    public void execute() {
        if (status != Status.PENDING) {
            switch (status) {
                case RUNNING:
                    throw new IllegalStateException("Cannot execute task:"
                            + " the task is already running.");
                case FINISHED:
                    throw new IllegalStateException("Cannot execute task:"
                            + " the task has already been executed "
                            + "(a task can be executed only once)");
            }
        }

        onPreExecute();
        status = Status.RUNNING;
        executeTask();
    }

    private void executeTask() {
        future = new FutureTask<Result>(worker) {
            @Override
            protected void done() {
                try {
                    postResultIfNotInvoked(get());
                } catch (InterruptedException e) {
                    android.util.Log.w(TAG, e);
                } catch (ExecutionException e) {
                    throw new RuntimeException("An error occurred while executing doInBackground()", e.getCause());
                } catch (CancellationException e) {
                    postResultIfNotInvoked(null);
                }
            }
        };
        executor.execute(future);
    }

    private void postResult(final Result result) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(!isCancelled()) {
                    onPostExecute(result);
                } else {
                    onCancelled();
                }
            }
        });
    }

    private void postResultIfNotInvoked(Result result) {
        final boolean wasTaskInvoked = taskInvoked.get();
        if (!wasTaskInvoked) {
            postResult(result);
        }
    }

    @Override
    protected void publishProgress(final Progress progress) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                onProgress(progress);
            }
        });
    }

    public void cancel(boolean mayInterruptIfRunning) {
        cancelled.set(true);
        future.cancel(mayInterruptIfRunning);
    }

    public final Result get() throws InterruptedException, ExecutionException {
        return future.get();
    }

    private static abstract class WorkerRunnable<Result> implements Callable<Result> { }

    public enum Status {
        /**
         * Indicates that the task has not been executed yet.
         */
        PENDING,
        /**
         * Indicates that the task is running.
         */
        RUNNING,
        /**
         * Indicates that {@link BaseAsyncTask#onPostExecute(Object)} has finished.
         */
        FINISHED,
    }
}
