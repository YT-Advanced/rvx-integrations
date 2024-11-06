package app.revanced.integrations.youtube.patches.components;

import androidx.annotation.Nullable;

import java.util.function.Supplier;
import java.util.stream.Stream;

import app.revanced.integrations.shared.patches.components.Filter;
import app.revanced.integrations.shared.patches.components.StringFilterGroup;
import app.revanced.integrations.shared.utils.Logger;
import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.youtube.shared.NavigationBar.NavigationButton;
import app.revanced.integrations.youtube.shared.RootView;

@SuppressWarnings("unused")
public final class CarouselShelfFilter extends Filter {
    private static final String BROWSE_ID_HOME = "FEwhat_to_watch";
    private static final String BROWSE_ID_LIBRARY = "FElibrary";
    private static final String BROWSE_ID_NOTIFICATION = "FEactivity";
    private static final String BROWSE_ID_NOTIFICATION_INBOX = "FEnotifications_inbox";
    private static final String BROWSE_ID_PLAYLIST = "VLPL";
    private static final String BROWSE_ID_SUBSCRIPTION = "FEsubscriptions";

    private static final Supplier<Stream<String>> knownBrowseId = () -> Stream.of(
            BROWSE_ID_HOME,
            BROWSE_ID_NOTIFICATION,
            BROWSE_ID_PLAYLIST,
            BROWSE_ID_SUBSCRIPTION
    );

    private static final Supplier<Stream<String>> whitelistBrowseId = () -> Stream.of(
            BROWSE_ID_LIBRARY,
            BROWSE_ID_NOTIFICATION_INBOX
    );

    public CarouselShelfFilter() {
        addPathCallbacks(
                new StringFilterGroup(
                        Settings.HIDE_CAROUSEL_SHELF,
                        "horizontal_video_shelf.eml",
                        "horizontal_shelf.eml",
                        "horizontal_shelf_inline.eml",
                        "horizontal_tile_shelf.eml"
                )
        );
    }

    private static boolean hideShelves(boolean playerActive, boolean searchBarActive, NavigationButton selectedNavButton, String browseId) {
        // Must check player type first, as search bar can be active behind the player.
        if (playerActive) {
            return false;
        }
        // Must check second, as search can be from any tab.
        if (searchBarActive) {
            return true;
        }
        // Unknown tab, treat the same as home.
        if (selectedNavButton == null) {
            return true;
        }
        return knownBrowseId.get().anyMatch(browseId::equals) || whitelistBrowseId.get().noneMatch(browseId::equals);
    }

    @Override
    public boolean isFiltered(String path, @Nullable String identifier, String allValue, byte[] protobufBufferArray,
                              StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        final boolean playerActive = RootView.isPlayerActive();
        final boolean searchBarActive = RootView.isSearchBarActive();
        final NavigationButton navigationButton = NavigationButton.getSelectedNavigationButton();
        final String navigation = navigationButton == null ? "null" : navigationButton.name();
        final String browseId = RootView.getBrowseId();
        final boolean hideShelves = hideShelves(playerActive, searchBarActive, navigationButton, browseId);
        if (contentIndex != 0) {
            return false;
        }
        Logger.printDebug(() -> "hideShelves: " + hideShelves + "\nplayerActive: " + playerActive + "\nsearchBarActive: " + searchBarActive + "\nbrowseId: " + browseId + "\nnavigation: " + navigation);
        if (!hideShelves) {
            return false;
        }
        return super.isFiltered(path, identifier, allValue, protobufBufferArray, matchedGroup, contentType, contentIndex);
    }
}
