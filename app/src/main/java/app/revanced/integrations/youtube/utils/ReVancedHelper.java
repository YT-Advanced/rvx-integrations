package app.revanced.integrations.youtube.utils;

import static app.revanced.integrations.youtube.utils.ResourceUtils.identifier;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.revanced.integrations.youtube.settings.SettingsEnum;

public class ReVancedHelper {
    public static String applicationLabel = "ReVanced_Extended";
    public static boolean isTablet = false;
    public static String packageName = "app.revanced.android.youtube";
    public static String appVersionName = "18.45.43";

    private ReVancedHelper() {
    } // utility class

    @Nullable
    private static PackageInfo getPackageInfo(@NonNull Context context) {
        try {
            final PackageManager packageManager = getPackageManager(context);
            final String packageName = context.getPackageName();
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    ? packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
                    : packageManager.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            LogHelper.printException(() -> "Failed to get package Info!" + e);
        }
        return null;
    }

    @NonNull
    private static PackageManager getPackageManager(@NonNull Context context) {
        return context.getPackageManager();
    }

    @NonNull
    public static String[] getStringArray(@NonNull Context context, @NonNull String key) {
        return context.getResources().getStringArray(identifier(key, ResourceType.ARRAY));
    }

    private static boolean isAdditionalSettingsEnabled() {
        // In the old player flyout panels, the video quality icon and additional quality icon are the same
        // Therefore, additional Settings should not be blocked in old player flyout panels
        if (isSpoofingToLessThan("18.22.00"))
            return false;

        boolean additionalSettingsEnabled = true;
        final SettingsEnum[] additionalSettings = {
                SettingsEnum.HIDE_PLAYER_FLYOUT_PANEL_AMBIENT,
                SettingsEnum.HIDE_PLAYER_FLYOUT_PANEL_HELP,
                SettingsEnum.HIDE_PLAYER_FLYOUT_PANEL_LOOP,
                SettingsEnum.HIDE_PLAYER_FLYOUT_PANEL_PREMIUM_CONTROLS,
                SettingsEnum.HIDE_PLAYER_FLYOUT_PANEL_STABLE_VOLUME,
                SettingsEnum.HIDE_PLAYER_FLYOUT_PANEL_STATS_FOR_NERDS,
                SettingsEnum.HIDE_PLAYER_FLYOUT_PANEL_WATCH_IN_VR,
                SettingsEnum.HIDE_PLAYER_FLYOUT_PANEL_YT_MUSIC,
        };
        for (SettingsEnum s : additionalSettings) {
            additionalSettingsEnabled &= s.getBoolean();
        }
        return additionalSettingsEnabled;
    }

    public static boolean isFullscreenHidden() {
        boolean isFullscreenHidden = isTablet &&
                !SettingsEnum.ENABLE_PHONE_LAYOUT.getBoolean();
        final SettingsEnum[] hideFullscreenSettings = {
                SettingsEnum.ENABLE_TABLET_LAYOUT,
                SettingsEnum.HIDE_QUICK_ACTIONS,
                SettingsEnum.HIDE_FULLSCREEN_PANELS
        };
        for (SettingsEnum s : hideFullscreenSettings) {
            isFullscreenHidden |= s.getBoolean();
        }
        return isFullscreenHidden;
    }

    public static boolean isPackageEnabled(@NonNull Context context, @NonNull String packageName) {
        try {
            return context.getPackageManager().getApplicationInfo(packageName, 0).enabled;
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        return false;
    }

    public static boolean isSpoofingToLessThan(@NonNull String versionName) {
        if (!SettingsEnum.SPOOF_APP_VERSION.getBoolean())
            return false;

        final int spoofedVersion = Integer.parseInt(SettingsEnum.SPOOF_APP_VERSION_TARGET.getString().replaceAll("\\.", ""));
        final int targetVersion = Integer.parseInt(versionName.replaceAll("\\.", ""));
        return spoofedVersion < targetVersion;
    }

    public static void setApplicationLabel(@NonNull Context context) {
        final PackageInfo packageInfo = getPackageInfo(context);
        if (packageInfo != null) {
            applicationLabel = (String) packageInfo.applicationInfo.loadLabel(getPackageManager(context));
        }
    }

    public static void setCommentPreviewSettings() {
        final boolean enabled = SettingsEnum.HIDE_PREVIEW_COMMENT.getBoolean();
        final boolean newMethod = SettingsEnum.HIDE_PREVIEW_COMMENT_TYPE.getBoolean();

        SettingsEnum.HIDE_PREVIEW_COMMENT_OLD_METHOD.saveValue(enabled && !newMethod);
        SettingsEnum.HIDE_PREVIEW_COMMENT_NEW_METHOD.saveValue(enabled && newMethod);
    }

    public static void setPlayerFlyoutPanelAdditionalSettings() {
        SettingsEnum.HIDE_PLAYER_FLYOUT_PANEL_ADDITIONAL_SETTINGS.saveValue(isAdditionalSettingsEnabled());
    }

    public static void setIsTablet(@NonNull Context context) {
        isTablet = context.getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }

    public static void setPackageName(@NonNull Context context) {
        packageName = context.getPackageName();
    }

    public static void setVersionName(@NonNull Context context) {
        final PackageInfo packageInfo = getPackageInfo(context);
        if (packageInfo != null) {
            appVersionName = packageInfo.versionName;
        }
    }
}