package com.nimbo.launcher;

import android.app.Activity;
import android.graphics.ImageDecoder;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

/** A bounded local background, with obsolete imports cancelled before replacing the saved image. */
final class WallpaperController {
    private static final int MAX_BYTES = 32 * 1024 * 1024;
    private final Activity activity;
    private final ImageView image;
    private final View shade;
    private final File saved;
    private final int widthLimit, heightLimit;
    private final LatestTaskRunner worker = new LatestTaskRunner();
    private Drawable current;
    private boolean visible;
    private volatile boolean closed;
    private volatile int generation;

    WallpaperController(Activity activity, ImageView image, View shade) {
        this.activity = activity;
        this.image = image;
        this.shade = shade;
        saved = new File(activity.getFilesDir(), "wallpaper.image");
        widthLimit = Math.min(1280, activity.getResources().getDisplayMetrics().widthPixels);
        heightLimit = Math.min(720, activity.getResources().getDisplayMetrics().heightPixels);
        worker.submit(this::cleanupPendingFiles);
    }

    boolean hasCustomWallpaper() { return saved.isFile(); }

    void load() {
        if (!saved.isFile()) return;
        final int expected = ++generation;
        worker.submit(() -> {
            try {
                cleanupPendingFiles();
                checkActive(expected);
                Drawable decoded = decode(saved);
                checkActive(expected);
                activity.runOnUiThread(() -> {
                    if (isCurrent(expected)) show(decoded);
                });
            } catch (InterruptedIOException ignored) {
                // Replaced by a newer request or the activity was destroyed.
            } catch (IOException | RuntimeException | OutOfMemoryError error) {
                reportFailure(expected, activity.getString(R.string.wallpaper_saved_open_failed), error);
            }
        });
    }

    void importImage(Uri uri) {
        final int expected = ++generation;
        toast(activity.getString(R.string.wallpaper_loading));
        worker.submit(() -> {
            File pending = null;
            boolean delivered = false;
            try {
                cleanupPendingFiles();
                checkActive(expected);
                // Unique across activity recreation: an old task cannot delete a new task's file.
                pending = File.createTempFile("wallpaper-", ".pending", activity.getFilesDir());
                try (InputStream in = activity.getContentResolver().openInputStream(uri);
                     FileOutputStream out = new FileOutputStream(pending)) {
                    if (in == null) throw new IOException("No input stream");
                    byte[] buffer = new byte[16384];
                    int count, total = 0;
                    while ((count = in.read(buffer)) != -1) {
                        checkActive(expected);
                        total += count;
                        if (total > MAX_BYTES) throw new IOException("Image exceeds local size limit");
                        out.write(buffer, 0, count);
                    }
                    out.getFD().sync();
                }
                checkActive(expected);
                Drawable decoded = decode(pending);
                checkActive(expected);
                final File ready = pending;
                activity.runOnUiThread(() -> {
                    try {
                        if (!isCurrent(expected)) return;
                        if (!ready.renameTo(saved)) {
                            toast(activity.getString(R.string.wallpaper_save_failed));
                            return;
                        }
                        show(decoded);
                        toast(activity.getString(R.string.wallpaper_applied));
                    } finally {
                        ready.delete();
                    }
                });
                delivered = true;
            } catch (InterruptedIOException ignored) {
                // Cancelled imports keep the previous saved wallpaper intact.
            } catch (IOException | RuntimeException | OutOfMemoryError error) {
                reportFailure(expected, activity.getString(R.string.wallpaper_import_failed), error);
            } finally {
                if (!delivered && pending != null) pending.delete();
            }
        });
    }

    private boolean isCurrent(int expected) { return !closed && expected == generation; }

    private void checkActive(int expected) throws InterruptedIOException {
        if (!isCurrent(expected) || Thread.currentThread().isInterrupted()) throw new InterruptedIOException();
    }

    private void cleanupPendingFiles() {
        // Fresh files may belong to a previous activity still finishing cancellation.
        long cutoff = System.currentTimeMillis() - 60 * 60 * 1000L;
        File[] pending = activity.getFilesDir().listFiles((dir, name) -> name.startsWith("wallpaper-") && name.endsWith(".pending"));
        if (pending != null) for (File file : pending) {
            if (Thread.currentThread().isInterrupted()) return;
            if (file.lastModified() < cutoff) file.delete();
        }
    }

    private Drawable decode(File file) throws IOException {
        Drawable decoded = ImageDecoder.decodeDrawable(ImageDecoder.createSource(file), (decoder, info, source) -> {
            int w = info.getSize().getWidth(), h = info.getSize().getHeight();
            float reduction = Math.min(1f, Math.min(widthLimit / (float) w, heightLimit / (float) h));
            decoder.setTargetSize(Math.max(1, Math.round(w * reduction)), Math.max(1, Math.round(h * reduction)));
            decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
        });
        if (decoded instanceof BitmapDrawable) ((BitmapDrawable) decoded).setFilterBitmap(false);
        if (decoded instanceof AnimatedImageDrawable)
            ((AnimatedImageDrawable) decoded).setRepeatCount(AnimatedImageDrawable.REPEAT_INFINITE);
        return decoded;
    }

    private void reportFailure(int expected, String message, Throwable error) {
        Log.w("HomeSimples", "Wallpaper operation failed", error);
        activity.runOnUiThread(() -> { if (isCurrent(expected)) toast(message); });
    }

    private void show(Drawable decoded) {
        stopAnimation();
        current = decoded;
        image.setImageDrawable(decoded);
        shade.setVisibility(View.VISIBLE);
        Log.i("HomeSimples", "wallpaper_loaded animated=" + (decoded instanceof AnimatedImageDrawable));
        if (visible) startAnimation();
    }

    void setVisible(boolean active) {
        visible = active;
        if (active) startAnimation();
        else stopAnimation();
    }

    private void startAnimation() {
        if (current instanceof AnimatedImageDrawable) {
            ((AnimatedImageDrawable) current).start();
            Log.i("HomeSimples", "gif_start");
        }
    }

    private void stopAnimation() {
        if (current instanceof AnimatedImageDrawable) {
            ((AnimatedImageDrawable) current).stop();
            Log.i("HomeSimples", "gif_stop");
        }
    }

    void clear(Runnable complete) {
        final int expected = ++generation;
        worker.submit(() -> {
            if (!isCurrent(expected) || Thread.currentThread().isInterrupted()) return;
            boolean removed = !saved.exists() || saved.delete();
            activity.runOnUiThread(() -> {
                if (!isCurrent(expected)) return;
                if (!removed) toast(activity.getString(R.string.wallpaper_remove_failed));
                else {
                    stopAnimation();
                    current = null;
                    image.setImageDrawable(null);
                    shade.setVisibility(View.GONE);
                    toast(activity.getString(R.string.wallpaper_original_restored));
                }
                complete.run();
            });
        });
    }

    void close() {
        closed = true;
        ++generation;
        stopAnimation();
        image.setImageDrawable(null);
        current = null;
        worker.close();
    }

    private void toast(String message) { Toast.makeText(activity, message, Toast.LENGTH_LONG).show(); }
}
