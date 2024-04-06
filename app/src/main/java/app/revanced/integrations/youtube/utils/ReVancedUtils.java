package app.revanced.integrations.youtube.utils;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.Bidi;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @noinspection ALL
 */
public class ReVancedUtils {
    /**
     * General purpose pool for network calls and other background tasks.
     * All tasks run at max thread priority.
     */
    private static final ThreadPoolExecutor backgroundThreadPool = new ThreadPoolExecutor(
            2, // 2 threads always ready to go
            Integer.MAX_VALUE,
            10, // For any threads over the minimum, keep them alive 10 seconds after they go idle
            TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            r -> { // ThreadFactory
                Thread t = new Thread(r);
                t.setPriority(Thread.MAX_PRIORITY); // run at max priority
                return t;
            });
    @SuppressLint("StaticFieldLeak")
    public static Context context;
    @Nullable
    private static Boolean isRightToLeftTextLayout;

    private ReVancedUtils() {
    } // utility class

    public interface MatchFilter<T> {
        boolean matches(T object);
    }

    /**
     * @param searchRecursively If children ViewGroups should also be
     *                          recursively searched using depth first search.
     * @return The first child view that matches the filter.
     */
    @Nullable
    public static <T extends View> T getChildView(@NonNull ViewGroup viewGroup, boolean searchRecursively,
                                                  @NonNull MatchFilter<View> filter) {
        for (int i = 0, childCount = viewGroup.getChildCount(); i < childCount; i++) {
            View childAt = viewGroup.getChildAt(i);
            if (filter.matches(childAt)) {
                return (T) childAt;
            }
            // Must do recursive after filter check, in case the filter is looking for a ViewGroup.
            if (searchRecursively && childAt instanceof ViewGroup) {
                T match = getChildView((ViewGroup) childAt, true, filter);
                if (match != null) return match;
            }
        }
        return null;
    }

    public static Context getContext() {
        return context;
    }

    public static void hideViewBy0dpUnderCondition(boolean condition, View view) {
        if (!condition) return;
        hideViewByLayoutParams(view);
    }

    public static void hideViewUnderCondition(boolean condition, View view) {
        if (!condition) return;
        view.setVisibility(View.GONE);
    }

    public static void hideViewByLayoutParams(View view) {
        if (view instanceof LinearLayout) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, 0);
            view.setLayoutParams(layoutParams);
        } else if (view instanceof FrameLayout) {
            FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(0, 0);
            view.setLayoutParams(layoutParams2);
        } else if (view instanceof RelativeLayout) {
            RelativeLayout.LayoutParams layoutParams3 = new RelativeLayout.LayoutParams(0, 0);
            view.setLayoutParams(layoutParams3);
        } else if (view instanceof Toolbar) {
            Toolbar.LayoutParams layoutParams4 = new Toolbar.LayoutParams(0, 0);
            view.setLayoutParams(layoutParams4);
        } else if (view instanceof ViewGroup) {
            ViewGroup.LayoutParams layoutParams5 = new ViewGroup.LayoutParams(0, 0);
            view.setLayoutParams(layoutParams5);
        }
    }

    public static boolean containsAny(@Nullable String value, @NonNull String... targets) {
        return value != null && indexOfFirstFound(value, targets) >= 0;
    }

    public static int indexOfFirstFound(@NonNull String value, @NonNull String... targets) {
        for (String string : targets) {
            if (!string.isEmpty()) {
                final int indexOf = value.indexOf(string);
                if (indexOf >= 0) return indexOf;
            }
        }
        return -1;
    }

    public static void runOnBackgroundThread(@NonNull Runnable task) {
        backgroundThreadPool.execute(task);
    }

    @NonNull
    public static <T> Future<T> submitOnBackgroundThread(@NonNull Callable<T> call) {
        return backgroundThreadPool.submit(call);
    }

    /**
     * If the device language uses right to left text layout (hebrew, arabic, etc)
     */
    public static boolean isRightToLeftTextLayout() {
        if (isRightToLeftTextLayout == null) {
            String displayLanguage = Locale.getDefault().getDisplayLanguage();
            isRightToLeftTextLayout = new Bidi(displayLanguage, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT).isRightToLeft();
        }
        return isRightToLeftTextLayout;
    }

    public static void setClipboard(@NonNull String text) {
        setClipboard(text, null);
    }

    public static void setClipboard(@NonNull String text, @Nullable String toastMessage) {
        if (!(context.getSystemService(Context.CLIPBOARD_SERVICE) instanceof ClipboardManager clipboard))
            return;
        final ClipData clip = ClipData.newPlainText(ReVancedHelper.applicationLabel, text);
        clipboard.setPrimaryClip(clip);

        // Do not show a toast if using Android 13+ as it shows it's own toast.
        // But if the user copied with a timestamp then show a toast.
        // Unfortunately this will show 2 toasts on Android 13+, but no way around this.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2 && toastMessage != null) {
            showToastShort(toastMessage);
        }
    }

    public static void showToastShort(Context context, String messageToToast) {
        showToast(context, messageToToast, Toast.LENGTH_SHORT);
    }

    public static void showToastLong(Context context, String messageToToast) {
        showToast(context, messageToToast, Toast.LENGTH_LONG);
    }

    /**
     * Safe to call from any thread
     */
    public static void showToastShort(String messageToToast) {
        showToast(context, messageToToast, Toast.LENGTH_SHORT);
    }

    /**
     * Safe to call from any thread
     */
    public static void showToastLong(String messageToToast) {
        showToast(context, messageToToast, Toast.LENGTH_LONG);
    }

    private static void showToast(Context context, String messageToToast, int toastDuration) {
        // cannot use getContext(), otherwise if context is null it will cause infinite recursion of error logging
        if (context == null || messageToToast == null || messageToToast.isEmpty())
            return;
        runOnMainThreadNowOrLater(() -> Toast.makeText(context, messageToToast, toastDuration).show());
    }

    /**
     * Automatically logs any exceptions the runnable throws.
     *
     * @see #runOnMainThreadNowOrLater(Runnable)
     */
    public static void runOnMainThread(@NonNull Runnable runnable) {
        runOnMainThreadDelayed(runnable, 0);
    }

    /**
     * Automatically logs any exceptions the runnable throws
     */
    public static void runOnMainThreadDelayed(@NonNull Runnable runnable, long delayMillis) {
        Runnable loggingRunnable = () -> {
            try {
                runnable.run();
            } catch (Exception ex) {
                LogHelper.printException(() -> runnable.getClass() + ": " + ex.getMessage(), ex);
            }
        };
        new Handler(Looper.getMainLooper()).postDelayed(loggingRunnable, delayMillis);
    }

    /**
     * If called from the main thread, the code is run immediately.<p>
     * If called off the main thread, this is the same as {@link #runOnMainThread(Runnable)}.
     */
    public static void runOnMainThreadNowOrLater(@NonNull Runnable runnable) {
        if (isCurrentlyOnMainThread()) {
            runnable.run();
        } else {
            runOnMainThread(runnable);
        }
    }

    /**
     * @return if the calling thread is on the main thread
     */
    public static boolean isCurrentlyOnMainThread() {
        return Looper.getMainLooper().isCurrentThread();
    }

    /**
     * @throws IllegalStateException if the calling thread is _off_ the main thread
     */
    public static void verifyOnMainThread() throws IllegalStateException {
        if (!isCurrentlyOnMainThread()) {
            throw new IllegalStateException("Must call _on_ the main thread");
        }
    }

    /**
     * @throws IllegalStateException if the calling thread is _on_ the main thread
     */
    public static void verifyOffMainThread() throws IllegalStateException {
        if (isCurrentlyOnMainThread()) {
            throw new IllegalStateException("Must call _off_ the main thread");
        }
    }

    public static boolean isNetworkNotConnected() {
        final NetworkType networkType = getNetworkType();
        return networkType == NetworkType.NONE;
    }

    @SuppressLint("MissingPermission") // permission already included in YouTube
    public static NetworkType getNetworkType() {
        if (context == null || !(context.getSystemService(Context.CONNECTIVITY_SERVICE) instanceof ConnectivityManager cm))
            return NetworkType.NONE;

        final NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected())
            return NetworkType.NONE;

        return switch (networkInfo.getType()) {
            case ConnectivityManager.TYPE_MOBILE, ConnectivityManager.TYPE_BLUETOOTH ->
                    NetworkType.MOBILE;
            default -> NetworkType.WIFI;
        };
    }

    public enum NetworkType {
        MOBILE("mobile"),
        WIFI("wifi"),
        NONE("none");

        private final String name;

        NetworkType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}