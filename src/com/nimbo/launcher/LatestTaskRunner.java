package com.nimbo.launcher;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/** One running task and at most one replacement, instead of an unbounded stale-work queue. */
final class LatestTaskRunner implements AutoCloseable {
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 15,
            TimeUnit.SECONDS, new ArrayBlockingQueue<>(1));
    private Future<?> latest;
    private boolean closed;

    LatestTaskRunner() { executor.allowCoreThreadTimeOut(true); }

    synchronized void submit(Runnable task) {
        if (closed) return;
        cancel();
        latest = executor.submit(task);
    }

    synchronized void cancel() {
        if (latest != null) latest.cancel(true);
        executor.purge();
        latest = null;
    }

    @Override public synchronized void close() {
        closed = true;
        cancel();
        executor.shutdownNow();
    }
}
