package app.revanced.integrations.youtube.patches.general;

import static app.revanced.integrations.youtube.utils.ReVancedUtils.hideViewBy0dpUnderCondition;
import static app.revanced.integrations.youtube.utils.ReVancedUtils.hideViewUnderCondition;
import static app.revanced.integrations.youtube.utils.ResourceUtils.identifier;
import static app.revanced.integrations.youtube.utils.StringRef.str;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import java.util.Objects;

import app.revanced.integrations.youtube.settings.SettingsEnum;
import app.revanced.integrations.youtube.utils.LogHelper;
import app.revanced.integrations.youtube.utils.ResourceType;
import app.revanced.integrations.youtube.utils.ReVancedUtils;

/**
 * @noinspection ALL
 */
@SuppressWarnings("unused")
public class GeneralPatch {
    private static final String MAIN_ACTIONS = "android.intent.action.MAIN";
    private static final String[] TOOLBAR_BUTTON_LIST = {
            "CREATION_ENTRY",   // Create button (Phone)
            "FAB_CAMERA",       // Create button (Tablet)
            "TAB_ACTIVITY"      // Notification button
    };
    private static final String PREMIUM_HEADER_NAME = "ytPremiumWordmarkHeader";
    private static FrameLayout.LayoutParams layoutParams;
    @NonNull
    private static String videoId = "";
    private static boolean subtitlePrefetched = true;
    private static int minimumHeight = 1;
    private static int paddingLeft = 12;
    private static int paddingTop = 0;
    private static int paddingRight = 12;
    private static int paddingBottom = 0;

    /**
     * Change the start page only when the user starts the app on the launcher.
     * <p>
     * If the app starts with a widget or the app starts through a shortcut,
     * Action of intent is not {@link #MAIN_ACTIONS}.
     *
     * @param intent original intent
     */
    public static void changeStartPage(@NonNull Intent intent) {
        if (!Objects.equals(intent.getAction(), MAIN_ACTIONS))
            return;

        final String startPage = SettingsEnum.CHANGE_START_PAGE.getString();
        if (startPage.isEmpty())
            return;

        if (startPage.startsWith("open.")) {
            intent.setAction("com.google.android.youtube.action." + startPage);
        } else if (startPage.startsWith("www.youtube.com")) {
            intent.setData(Uri.parse(startPage));
        } else {
            ReVancedUtils.showToastShort(str("revanced_change_start_page_warning"));
            SettingsEnum.CHANGE_START_PAGE.resetToDefault();
            return;
        }
        LogHelper.printDebug(() -> "Changing start page to " + startPage);
    }

    /**
     * Injection point.
     * <p>
     * The {@link AlertDialog#getButton(int)} method must be used after {@link AlertDialog#show()} is called.
     * Otherwise {@link AlertDialog#getButton(int)} method will always return null.
     * https://stackoverflow.com/a/4604145
     * <p>
     * That's why {@link AlertDialog#show()} is absolutely necessary.
     * Instead, use two tricks to hide Alertdialog.
     * <p>
     * 1. Change the size of AlertDialog to 0.
     * 2. Disable AlertDialog's background dim.
     * <p>
     * This way, AlertDialog will be completely hidden,
     * and {@link AlertDialog#getButton(int)} method can be used without issue.
     */
    public static void confirmDialog(final AlertDialog dialog) {
        if (!SettingsEnum.REMOVE_VIEWER_DISCRETION_DIALOG.getBoolean()) {
            return;
        }

        // This method is called after AlertDialog#show(),
        // So we need to hide the AlertDialog before pressing the possitive button.
        final Window window = dialog.getWindow();
        final Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (window != null && button != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.height = 0;
            params.width = 0;

            // Change the size of AlertDialog to 0.
            window.setAttributes(params);

            // Disable AlertDialog's background dim.
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            button.setSoundEffectsEnabled(false);
            button.performClick();
        }
    }

    public static void confirmDialogAgeVerified(final AlertDialog dialog) {
        final Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (!button.getText().toString().equals(str("og_continue")))
            return;

        confirmDialog(dialog);
    }

    public static boolean disableAutoCaptions(boolean original) {
        if (!SettingsEnum.DISABLE_AUTO_CAPTIONS.getBoolean())
            return original;

        return subtitlePrefetched;
    }

    public static boolean disableRollingNumberAnimations() {
        return SettingsEnum.DISABLE_ROLLING_NUMBER_ANIMATIONS.getBoolean();
    }

    public static boolean enableGradientLoadingScreen() {
        return SettingsEnum.ENABLE_GRADIENT_LOADING_SCREEN.getBoolean();
    }

    public static int enablePremiumHeader(int originalValue) {
        if (SettingsEnum.ENABLE_PREMIUM_HEADER.getBoolean())
            return identifier(PREMIUM_HEADER_NAME, ResourceType.ATTR);

        return originalValue;
    }

    public static boolean enableSongSearch() {
        return SettingsEnum.ENABLE_SONG_SEARCH.getBoolean();
    }

    public static boolean enableTabletMiniPlayer(boolean original) {
        return SettingsEnum.ENABLE_TABLET_MINI_PLAYER.getBoolean() || original;
    }

    public static boolean enableWideSearchBar(boolean original) {
        return SettingsEnum.ENABLE_WIDE_SEARCH_BAR.getBoolean() || original;
    }

    public static boolean enableWideSearchBarInYouTab(boolean original) {
        if (!SettingsEnum.ENABLE_WIDE_SEARCH_BAR.getBoolean())
            return original;
        else
            return !SettingsEnum.ENABLE_WIDE_SEARCH_BAR_IN_YOU_TAB.getBoolean() && original;
    }

    public static void hideAccountList(View view, CharSequence charSequence) {
        if (!SettingsEnum.HIDE_ACCOUNT_MENU.getBoolean())
            return;

        if (!(view.getParent().getParent().getParent() instanceof ViewGroup viewGroup))
            return;

        String[] blockList = SettingsEnum.HIDE_ACCOUNT_MENU_FILTER_STRINGS.getString().split("\\n");
        String targetString = charSequence.toString();

        for (String filter : blockList) {
            if (targetString.equals(filter) && !filter.isEmpty()) {
                viewGroup.setLayoutParams(new LayoutParams(0, 0));
            }
        }
    }

    public static void hideAccountMenu(View view, CharSequence charSequence) {
        if (!SettingsEnum.HIDE_ACCOUNT_MENU.getBoolean())
            return;

        if (!(view.getParent().getParent() instanceof ViewGroup viewGroup))
            return;

        String[] blockList = SettingsEnum.HIDE_ACCOUNT_MENU_FILTER_STRINGS.getString().split("\\n");
        String targetString = charSequence.toString();

        for (String filter : blockList) {
            if (targetString.equals(filter) && !filter.isEmpty()) {
                if (viewGroup.getLayoutParams() instanceof MarginLayoutParams)
                    hideAccountMenu(viewGroup);
                else
                    viewGroup.setLayoutParams(new LayoutParams(0, 0));
            }
        }
    }

    private static void hideAccountMenu(ViewGroup viewGroup) {
        viewGroup.setVisibility(View.GONE);
    }

    public static boolean hideAutoPlayerPopupPanels() {
        return SettingsEnum.HIDE_AUTO_PLAYER_POPUP_PANELS.getBoolean();
    }

    public static int hideCastButton(int original) {
        return SettingsEnum.HIDE_CAST_BUTTON.getBoolean() ? View.GONE : original;
    }

    public static int hideCategoryBarInFeed(int original) {
        return SettingsEnum.HIDE_CATEGORY_BAR_IN_FEED.getBoolean() ? 0 : original;
    }

    public static void hideCategoryBarInRelatedVideo(View view) {
        hideViewBy0dpUnderCondition(SettingsEnum.HIDE_CATEGORY_BAR_IN_RELATED_VIDEO.getBoolean(), view);
    }

    public static int hideCategoryBarInSearchResults(int original) {
        return SettingsEnum.HIDE_CATEGORY_BAR_IN_SEARCH_RESULTS.getBoolean() ? 0 : original;
    }

    public static void hideChannelListSubMenu(View view) {
        hideViewUnderCondition(SettingsEnum.HIDE_CHANNEL_LIST_SUBMENU.getBoolean(), view);
    }

    public static void hideCrowdfundingBox(View view) {
        hideViewBy0dpUnderCondition(SettingsEnum.HIDE_CROWDFUNDING_BOX.getBoolean(), view);
    }

    public static boolean hideFloatingMicrophone(boolean original) {
        return SettingsEnum.HIDE_FLOATING_MICROPHONE.getBoolean() || original;
    }

    public static int hideHandle(int originalValue) {
        return SettingsEnum.HIDE_HANDLE.getBoolean() ? 8 : originalValue;
    }

    public static void hideLatestVideosButton(View view) {
        hideViewUnderCondition(SettingsEnum.HIDE_LATEST_VIDEOS_BUTTON.getBoolean(), view);
    }

    public static void hideLoadMoreButton(View view) {
        if (!SettingsEnum.HIDE_LOAD_MORE_BUTTON.getBoolean())
            return;

        if (!(view instanceof ViewGroup viewGroup))
            return;

        if (!(viewGroup.getChildAt(0) instanceof ViewGroup expandButtonContainer))
            return;

        if (layoutParams == null
                && expandButtonContainer.getLayoutParams() instanceof FrameLayout.LayoutParams lp) {
            layoutParams = lp;
            paddingLeft = view.getPaddingLeft();
            paddingTop = view.getPaddingTop();
            paddingRight = view.getPaddingRight();
            paddingBottom = view.getPaddingBottom();
        }

        ReVancedUtils.runOnMainThreadDelayed(() -> {
                    if (minimumHeight == 1) {
                        minimumHeight = view.getMinimumHeight();
                    }
                    if (expandButtonContainer.getChildAt(0).getVisibility() != View.VISIBLE && layoutParams != null) {
                        view.setMinimumHeight(minimumHeight);
                        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
                        expandButtonContainer.setLayoutParams(layoutParams);
                    } else {
                        view.setMinimumHeight(0);
                        view.setPadding(0, 0, 0, 0);
                        expandButtonContainer.setLayoutParams(new FrameLayout.LayoutParams(0, 0));
                    }
                },
                0
        );
    }

    public static boolean hideSearchTermThumbnail() {
        return SettingsEnum.HIDE_SEARCH_TERM_THUMBNAIL.getBoolean();
    }


    public static boolean hideSnackBar() {
        return SettingsEnum.HIDE_SNACK_BAR.getBoolean();
    }

    public static void hideToolBarButton(String enumString, View view) {
        if (!SettingsEnum.HIDE_TOOLBAR_CREATE_NOTIFICATION_BUTTON.getBoolean())
            return;

        hideViewUnderCondition(
                ReVancedUtils.containsAny(enumString, TOOLBAR_BUTTON_LIST),
                view
        );
    }

    public static void hideTrendingSearches(ImageView imageView, boolean isTrendingSearches) {
        View parent = (View) imageView.getParent();

        if (SettingsEnum.HIDE_TRENDING_SEARCHES.getBoolean() && isTrendingSearches)
            parent.setVisibility(View.GONE);
        else
            parent.setVisibility(View.VISIBLE);

    }

    public static void newVideoStarted(@NonNull String newlyLoadedVideoId) {
        if (Objects.equals(newlyLoadedVideoId, videoId)) {
            return;
        }
        videoId = newlyLoadedVideoId;
        subtitlePrefetched = false;
    }

    public static void prefetchSubtitleTrack() {
        subtitlePrefetched = true;
    }
}
