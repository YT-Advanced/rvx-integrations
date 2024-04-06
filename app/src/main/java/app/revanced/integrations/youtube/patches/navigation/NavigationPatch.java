package app.revanced.integrations.youtube.patches.navigation;

import static app.revanced.integrations.youtube.utils.ReVancedUtils.hideViewUnderCondition;

import android.view.View;
import android.widget.TextView;

import java.util.EnumMap;
import java.util.Map;

import app.revanced.integrations.youtube.patches.utils.NavigationBarHookPatch.NavigationButton;

import app.revanced.integrations.youtube.settings.SettingsEnum;

@SuppressWarnings("unused")
public class NavigationPatch {

    private static final Map<NavigationButton, Boolean> shouldHideMap = new EnumMap<>(NavigationButton.class) {
        {
            put(NavigationButton.HOME, SettingsEnum.HIDE_HOME_BUTTON.getBoolean());
            put(NavigationButton.SHORTS, SettingsEnum.HIDE_SHORTS_BUTTON.getBoolean());
            put(NavigationButton.SUBSCRIPTIONS, SettingsEnum.HIDE_SUBSCRIPTIONS_BUTTON.getBoolean());
            put(NavigationButton.CREATE, SettingsEnum.HIDE_CREATE_BUTTON.getBoolean());
            put(NavigationButton.NOTIFICATIONS, SettingsEnum.HIDE_NOTIFICATIONS_BUTTON.getBoolean());

            put(NavigationButton.LIBRARY_LOGGED_OUT, SettingsEnum.HIDE_LIBRARY_BUTTON.getBoolean());
            put(NavigationButton.LIBRARY_INCOGNITO, SettingsEnum.HIDE_LIBRARY_BUTTON.getBoolean());
            put(NavigationButton.LIBRARY_OLD_UI, SettingsEnum.HIDE_LIBRARY_BUTTON.getBoolean());
            put(NavigationButton.LIBRARY_PIVOT_UNKNOWN, SettingsEnum.HIDE_LIBRARY_BUTTON.getBoolean());
            put(NavigationButton.LIBRARY_YOU, SettingsEnum.HIDE_LIBRARY_BUTTON.getBoolean());
        }
    };

    public static boolean switchCreateNotification(boolean original) {
        return SettingsEnum.SWITCH_CREATE_NOTIFICATION.getBoolean() || original;
    }

    public static void hideNavigationLabel(TextView view) {
        hideViewUnderCondition(SettingsEnum.HIDE_NAVIGATION_LABEL.getBoolean(), view);
    }

    public static boolean enableTabletNavBar(boolean original) {
        return SettingsEnum.ENABLE_TABLET_NAVIGATION_BAR.getBoolean() || original;
    }

    /**
     * Injection point.
     */
    public static void navigationTabCreated(NavigationButton button, View tabView) {
        if (Boolean.TRUE.equals(shouldHideMap.get(button))) {
            tabView.setVisibility(View.GONE);
        }
    }
}
