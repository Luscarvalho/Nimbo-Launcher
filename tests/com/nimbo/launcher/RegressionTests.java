package com.nimbo.launcher;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** JVM regressions for persisted order/hidden apps and cancellation under a slow background task. */
public final class RegressionTests {
    private static int checks;
    private static void equal(Object expected, Object actual) {
        checks++;
        if (!expected.equals(actual)) throw new AssertionError("Expected " + expected + ", got " + actual);
    }
    private static void await(CountDownLatch latch) throws InterruptedException {
        if (!latch.await(5, TimeUnit.SECONDS)) throw new AssertionError("Timed out waiting for worker");
    }

    private static void orderAndHiddenApps() {
        List<String> installed = Arrays.asList("video", "store", "music", "new-app");
        String saved = "music,video,store,uninstalled";
        List<String> hidden = AppOrder.visible(installed, Collections.singleton("video"), saved);
        equal(Arrays.asList("music", "store", "new-app"), hidden);
        // Reordering while one app is hidden must not erase that app from the persisted order.
        List<String> moved = AppOrder.move(hidden, "store", 0);
        equal(Arrays.asList("store", "music", "new-app"), moved);
        equal(Arrays.asList("music", "store", "new-app"), hidden);
        String updated = AppOrder.save(moved, saved);
        equal("store,music,new-app,video,uninstalled", updated);
        equal(Arrays.asList("store", "music", "new-app", "video"),
                AppOrder.visible(installed, Collections.emptySet(), updated));
        // A missing package remains restorable, but never creates a ghost card.
        equal(Arrays.asList("store", "music", "new-app", "video", "uninstalled"),
                AppOrder.visible(Arrays.asList("video", "store", "music", "new-app", "uninstalled"), Collections.emptySet(), updated));
        equal(Arrays.asList("video", "store", "music", "new-app"),
                AppOrder.visible(installed, Collections.emptySet(), "video,video,missing"));
        equal(Arrays.asList("video", "music", "new-app", "store"), AppOrder.move(installed, "store", 999));
        equal(installed, AppOrder.move(installed, "missing", 0));
        equal(Arrays.asList("store", "video", "music", "new-app"), AppOrder.move(installed, "store", -1));
        equal(Collections.emptyList(), AppOrder.visible(Collections.emptyList(), Collections.singleton("video"), saved));
    }

    private static void latestRequestWins() throws Exception {
        LatestTaskRunner runner = new LatestTaskRunner();
        CountDownLatch started = new CountDownLatch(1), release = new CountDownLatch(1), finished = new CountDownLatch(1);
        AtomicInteger executed = new AtomicInteger(), selected = new AtomicInteger(-1), interrupted = new AtomicInteger();
        try {
            runner.submit(() -> {
                started.countDown();
                // Models a native decoder that needs time to return even after interruption.
                boolean waiting = true;
                while (waiting) try { release.await(); waiting = false; }
                catch (InterruptedException ignored) { interrupted.incrementAndGet(); }
            });
            await(started);
            for (int i = 0; i < 1000; i++) {
                final int number = i;
                runner.submit(() -> { selected.set(number); executed.incrementAndGet(); finished.countDown(); });
            }
            release.countDown();
            await(finished);
            equal(999, selected.get());
            equal(1, executed.get());
            equal(true, interrupted.get() > 0);
        } finally { release.countDown(); runner.close(); }
    }

    private static void closeCancelsWork() throws Exception {
        LatestTaskRunner runner = new LatestTaskRunner();
        CountDownLatch started = new CountDownLatch(1), stopped = new CountDownLatch(1);
        AtomicInteger unexpected = new AtomicInteger();
        runner.submit(() -> {
            started.countDown();
            try { new CountDownLatch(1).await(); }
            catch (InterruptedException expected) { stopped.countDown(); }
        });
        await(started);
        runner.close();
        await(stopped);
        runner.submit(unexpected::incrementAndGet);
        equal(0, unexpected.get());
        runner.close();
    }

    public static void main(String[] args) throws Exception {
        orderAndHiddenApps();
        latestRequestWins();
        closeCancelsWork();
        System.out.println("PASS: " + checks + " regression checks");
    }
}
