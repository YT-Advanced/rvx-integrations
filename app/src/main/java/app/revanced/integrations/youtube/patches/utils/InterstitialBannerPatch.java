package app.revanced.integrations.youtube.patches.utils;

import static app.revanced.integrations.youtube.utils.StringRef.str;

import android.app.Instrumentation;
import android.view.KeyEvent;

import app.revanced.integrations.youtube.settings.SettingsEnum;
import app.revanced.integrations.youtube.utils.ReVancedUtils;

public class InterstitialBannerPatch {
    /**
     * Last time method is called
     */
    private static volatile long lastTimeCalled = 0;
    private static final Instrumentation instrumentation = new Instrumentation();

    public static void onBackPressed() {
        final long currentTime = System.currentTimeMillis();
        if (lastTimeCalled != 0 && currentTime - lastTimeCalled < 10000)
            return;

        lastTimeCalled = currentTime;
        ReVancedUtils.runOnMainThreadDelayed(() -> {
            ReVancedUtils.runOnBackgroundThread(() -> {
                try {
                    instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
                } catch (Exception ex) {
                    // Injecting user events on Android 10+ requires the manifest to include
                    // INJECT_EVENTS, and it's usage is heavily restricted
                    // and requires the user to manually approve the permission in the device settings.
                    //
                    // And no matter what, permissions cannot be added for root installations
                    // as manifest changes are ignored for mount installations.
                    //
                    // Instead, catch the SecurityException and turn off hide full screen ads
                    // since this functionality does not work for these devices.
                    SettingsEnum.CLOSE_INTERSTITIAL_ADS.saveValue(false);
                }
            });
        }, 1000);
        ReVancedUtils.runOnMainThreadDelayed(() -> ReVancedUtils.showToastShort(str("revanced_close_interstitial_ads_toast")), 1000);
    }
}
