package com.nimbo.launcher;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Main-thread bindings, a 1 MiB cache and only one native decode in flight. */
final class ThumbnailLoader implements AutoCloseable {
    private final ContentResolver resolver;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final Map<ImageView, Uri> bindings = new WeakHashMap<>();
    private final LinkedHashSet<Uri> pending = new LinkedHashSet<>();
    private final LruCache<Uri, Bitmap> cache = new LruCache<Uri, Bitmap>(1024 * 1024) {
        @Override protected int sizeOf(Uri key, Bitmap value) { return value.getAllocationByteCount(); }
    };
    private Uri running;
    private int generation, runningGeneration;
    private boolean closed;

    ThumbnailLoader(ContentResolver resolver) { this.resolver = resolver; }

    void bind(ImageView view, Uri uri) {
        if (closed) return;
        bindings.put(view, uri);
        view.setTag(uri);
        Bitmap cached = cache.get(uri);
        view.setImageBitmap(cached);
        // Recycled rows withdraw their old requests before another decode is scheduled.
        pending.removeIf(key -> !bindings.containsValue(key));
        if (cached == null && (!uri.equals(running) || runningGeneration != generation)) pending.add(uri);
        // Even a provider/adapter behaving unusually cannot accumulate an unlimited queue.
        while (pending.size() > 32) pending.remove(pending.iterator().next());
        startNext();
    }

    private void startNext() {
        if (closed || running != null) return;
        pending.removeIf(key -> !bindings.containsValue(key));
        Iterator<Uri> iterator = pending.iterator();
        if (!iterator.hasNext()) return;
        final Uri uri = iterator.next();
        iterator.remove();
        running = uri;
        final int expected = generation;
        runningGeneration = expected;
        worker.execute(() -> {
            Bitmap decoded = null;
            try {
                if (!Thread.currentThread().isInterrupted())
                    decoded = ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri), (decoder, info, source) -> {
                        float reduction = Math.min(1f, Math.min(240f / info.getSize().getWidth(), 135f / info.getSize().getHeight()));
                        decoder.setTargetSize(Math.max(1, Math.round(info.getSize().getWidth() * reduction)),
                                Math.max(1, Math.round(info.getSize().getHeight() * reduction)));
                    });
            } catch (IOException | RuntimeException | OutOfMemoryError error) {
                Log.w("HomeSimples", "Unable to load image preview", error);
            }
            final Bitmap thumbnail = decoded;
            main.post(() -> {
                running = null;
                if (closed) return;
                if (expected == generation && thumbnail != null) {
                    cache.put(uri, thumbnail);
                    for (Map.Entry<ImageView, Uri> binding : bindings.entrySet()) {
                        ImageView view = binding.getKey();
                        if (view != null && uri.equals(binding.getValue())) view.setImageBitmap(thumbnail);
                    }
                }
                startNext();
            });
        });
    }

    void reset() {
        ++generation;
        pending.clear();
        bindings.clear();
        cache.evictAll();
        // A native decode already running may finish; its old generation is never displayed.
    }

    @Override public void close() {
        closed = true;
        reset();
        worker.shutdownNow();
        main.removeCallbacksAndMessages(null);
    }
}
