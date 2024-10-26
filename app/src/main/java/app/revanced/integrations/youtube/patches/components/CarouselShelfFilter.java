package app.revanced.integrations.youtube.patches.components;

import androidx.annotation.Nullable;

import java.util.function.Supplier;
import java.util.stream.Stream;

import app.revanced.integrations.shared.patches.components.Filter;
import app.revanced.integrations.shared.patches.components.StringFilterGroup;
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

    private static boolean hideShelves(NavigationButton selectedNavButton, String browseId) {
        // Must check player type first, as search bar can be active behind the player.
        if (RootView.isPlayerActive()) {
            return false;
        }
        // Must check second, as search can be from any tab.
        if (RootView.isSearchBarActive()) {
            return true;
        }
        // Unknown tab, treat the same as home.
        if (selectedNavButton == null) {
            return true;
        }
        if (knownBrowseId.get().anyMatch(browseId::equals)) {
            return true;
        }
        return whitelistBrowseId.get().noneMatch(browseId::equals);
    }

    @Override
    public boolean isFiltered(String path, @Nullable String identifier, String allValue, byte[] protobufBufferArray,
                              StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        if (contentIndex == 0 && hideShelves(NavigationButton.getSelectedNavigationButton(), RootView.getBrowseId())) {
            return super.isFiltered(path, identifier, allValue, protobufBufferArray, matchedGroup, contentType, contentIndex);
        }

        return false;
    }
}
