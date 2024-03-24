package app.revanced.integrations.youtube.patches.components;

import androidx.annotation.Nullable;

import app.revanced.integrations.youtube.settings.SettingsEnum;
import app.revanced.integrations.youtube.shared.PlayerType;
import app.revanced.integrations.youtube.patches.utils.BrowseIdPatch;

/**
 * @noinspection rawtypes
 */
@SuppressWarnings("unused")
public final class LayoutComponentsFilter extends Filter {
    private static final String ENDORSEMENT_HEADER_FOOTER_PATH = "endorsement_header_footer";
    private static final ByteArrayAsStringFilterGroup grayDescriptionIdentifier =
            new ByteArrayAsStringFilterGroup(
                    SettingsEnum.HIDE_VIDEO_WITH_GRAY_DESCRIPTION,
                    ENDORSEMENT_HEADER_FOOTER_PATH
            );
    private static final ByteArrayAsStringFilterGroup lowViewsVideoIdentifier =
            new ByteArrayAsStringFilterGroup(
                    SettingsEnum.HIDE_VIDEO_WITH_LOW_VIEW,
                    "g-highZ"
            );
    private static final ByteArrayAsStringFilterGroup membershipVideoIdentifier =
            new ByteArrayAsStringFilterGroup(
                    SettingsEnum.HIDE_HOME_FEED_MEMBERSHIP_VIDEO,
                    "high-ptsZ"
            );
    private final StringFilterGroup communityPosts;
    private final StringFilterGroupList communityPostsGroupList = new StringFilterGroupList();
    private final StringFilterGroup homeVideoWithContext;
    private final StringFilterGroup searchVideoWithContext;

    public LayoutComponentsFilter() {
        // Identifiers.

        final StringFilterGroup graySeparator = new StringFilterGroup(
                SettingsEnum.HIDE_GRAY_SEPARATOR,
                "cell_divider"
        );

        final StringFilterGroup chipsShelf = new StringFilterGroup(
                SettingsEnum.HIDE_CHIPS_SHELF,
                "chips_shelf"
        );

        final StringFilterGroup searchBar = new StringFilterGroup(
                SettingsEnum.HIDE_SEARCH_BAR,
                "search_bar_entry_point"
        );

        identifierFilterGroupList.addAll(
                chipsShelf,
                graySeparator,
                searchBar
        );

        // Paths.

        final StringFilterGroup albumCard = new StringFilterGroup(
                SettingsEnum.HIDE_ALBUM_CARDS,
                "browsy_bar",
                "official_card"
        );

        final StringFilterGroup audioTrackButton = new StringFilterGroup(
                SettingsEnum.HIDE_AUDIO_TRACK_BUTTON,
                "multi_feed_icon_button"
        );

        communityPosts = new StringFilterGroup(
                null,
                "post_base_wrapper",
                "image_post_root"
        );

        final StringFilterGroup expandableMetadata = new StringFilterGroup(
                SettingsEnum.HIDE_EXPANDABLE_CHIP,
                "inline_expander"
        );

        final StringFilterGroup feedSurvey = new StringFilterGroup(
                SettingsEnum.HIDE_FEED_SURVEY,
                "feed_nudge",
                "infeed_survey",
                "in_feed_survey",
                "slimline_survey"
        );

        final StringFilterGroup grayDescription = new StringFilterGroup(
                SettingsEnum.HIDE_GRAY_DESCRIPTION,
                ENDORSEMENT_HEADER_FOOTER_PATH
        );

        homeVideoWithContext = new StringFilterGroup(
                SettingsEnum.HIDE_VIDEO_WITH_LOW_VIEW
                    || SettingsEnum.HIDE_HOME_FEED_MEMBERSHIP_VIDEO,
                "home_video_with_context.eml"
        );

        final StringFilterGroup infoPanel = new StringFilterGroup(
                SettingsEnum.HIDE_INFO_PANEL,
                "compact_banner",
                "publisher_transparency_panel",
                "single_item_information_panel"
        );

        final StringFilterGroup latestPosts = new StringFilterGroup(
                SettingsEnum.HIDE_LATEST_POSTS,
                "post_shelf"
        );

        final StringFilterGroup medicalPanel = new StringFilterGroup(
                SettingsEnum.HIDE_MEDICAL_PANEL,
                "emergency_onebox",
                "medical_panel"
        );

        final StringFilterGroup movieShelf = new StringFilterGroup(
                SettingsEnum.HIDE_MOVIE_SHELF,
                "compact_movie",
                "horizontal_movie_shelf",
                "movie_and_show_upsell_card",
                "compact_tvfilm_item",
                "offer_module"
        );

        final StringFilterGroup notifyMe = new StringFilterGroup(
                SettingsEnum.HIDE_NOTIFY_ME_BUTTON,
                "set_reminder_button"
        );

        searchVideoWithContext = new StringFilterGroup(
                SettingsEnum.HIDE_VIDEO_WITH_GRAY_DESCRIPTION,
                "search_video_with_context.eml"
        );

        final StringFilterGroup ticketShelf = new StringFilterGroup(
                SettingsEnum.HIDE_TICKET_SHELF,
                "ticket_horizontal_shelf",
                "ticket_shelf"
        );

        final StringFilterGroup timedReactions = new StringFilterGroup(
                SettingsEnum.HIDE_TIMED_REACTIONS,
                "emoji_control_panel",
                "timed_reaction"
        );

        pathFilterGroupList.addAll(
                albumCard,
                audioTrackButton,
                communityPosts,
                expandableMetadata,
                feedSurvey,
                grayDescription,
                homeVideoWithContext,
                infoPanel,
                latestPosts,
                medicalPanel,
                movieShelf,
                notifyMe,
                searchVideoWithContext,
                ticketShelf,
                timedReactions
        );

        communityPostsGroupList.addAll(
                new StringFilterGroup(
                        SettingsEnum.HIDE_COMMUNITY_POSTS_HOME,
                        "horizontalCollectionSwipeProtector=null"
                ),
                new StringFilterGroup(
                        SettingsEnum.HIDE_COMMUNITY_POSTS_SUBSCRIPTIONS,
                        "heightConstraint=null"
                )
        );
    }

    @Override
    boolean isFiltered(String path, @Nullable String identifier, String allValue, byte[] protobufBufferArray,
                       FilterGroupList matchedList, FilterGroup matchedGroup, int matchedIndex) {
        if (matchedGroup == homeVideoWithContext)
            return (membershipVideoIdentifier.check(protobufBufferArray).isFiltered()
                    || lowViewsVideoIdentifier.check(protobufBufferArray).isFiltered());
        if (matchedGroup == searchVideoWithContext) {
            return grayDescriptionIdentifier.check(protobufBufferArray).isFiltered();
        }
        if (matchedGroup == communityPosts) {
            if (PlayerType.getCurrent() == PlayerType.WATCH_WHILE_MAXIMIZED)
                return SettingsEnum.HIDE_COMMUNITY_POSTS_RELATED_VIDEO.getBoolean();

            // YouTube is testing new community post component in home feed
            if (BrowseIdPatch.isHomeFeed())
                return SettingsEnum.HIDE_COMMUNITY_POSTS_HOME.getBoolean();
            return communityPostsGroupList.check(allValue).isFiltered();
        }

        return super.isFiltered(path, identifier, allValue, protobufBufferArray, matchedList, matchedGroup, matchedIndex);
    }
}
