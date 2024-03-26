package app.revanced.integrations.youtube.patches.components;

import androidx.annotation.Nullable;

import app.revanced.integrations.youtube.settings.SettingsEnum;
import app.revanced.integrations.youtube.utils.StringTrieSearch;

/**
 * @noinspection rawtypes
 */
@SuppressWarnings("unused")
public final class ShortsFilter extends Filter {
    private static final String REEL_CHANNEL_BAR_PATH = "reel_channel_bar.eml";
    private static final String SHORTS_SHELF_HEADER_CONVERSION_CONTEXT = "horizontalCollectionSwipeProtector=null";

    private final StringFilterGroup shortsCompactFeedVideoPath;
    private final ByteArrayAsStringFilterGroup shortsCompactFeedVideoBuffer;

    private final StringTrieSearch exceptions = new StringTrieSearch();
    private final StringFilterGroup infoPanel;
    private final StringFilterGroup shelfHeader;

    private final StringFilterGroup videoActionButton;
    private final ByteArrayFilterGroupList videoActionButtonGroupList = new ByteArrayFilterGroupList();


    public ShortsFilter() {
        exceptions.addPatterns(
                "lock_mode_suggested_action"
        );

        final StringFilterGroup thanksButton = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS_PLAYER_THANKS_BUTTON,
                "suggested_action"
        );

        // Feed Shorts shelf header.
        // Use a different filter group for this pattern, as it requires an additional check after matching.
        shelfHeader = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS_SHELF,
                "shelf_header.eml"
        );

        final StringFilterGroup shorts = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS_SHELF,
                "shorts_shelf",
                "inline_shorts",
                "shorts_grid",
                "shorts_video_cell"
        );


        identifierFilterGroupList.addAll(
                shelfHeader,
                shorts,
                thanksButton
        );

        // Shorts that appear in the feed/search when the device is using tablet layout.
        shortsCompactFeedVideoPath = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS_SHELF,
                "compact_video.eml"
        );

        // Filter out items that use the 'frame0' thumbnail.
        // This is a valid thumbnail for both regular videos and Shorts,
        // but it appears these thumbnails are used only for Shorts.
        shortsCompactFeedVideoBuffer = new ByteArrayAsStringFilterGroup(
                SettingsEnum.HIDE_SHORTS_SHELF,
                "/frame0.jpg"
        );

        final StringFilterGroup joinButton = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS_PLAYER_JOIN_BUTTON,
                "sponsor_button"
        );

        final StringFilterGroup reelSoundMetadata = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS_PLAYER_SOUND_METADATA_LABEL,
                "reel_sound_metadata"
        );

        final StringFilterGroup subscribeButton = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS_PLAYER_SUBSCRIPTIONS_BUTTON,
                "shorts_paused_state",
                "subscribe_button"
        );

        infoPanel = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS_PLAYER_INFO_PANEL,
                "reel_multi_format_link",
                "reel_sound_metadata",
                "shorts_info_panel_overview"
        );

        videoActionButton = new StringFilterGroup(
                null,
                "shorts_video_action_button"
        );

        final StringFilterGroup videoLinkLabel = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS_PLAYER_VIDEO_LINK_LABEL,
                "reel_multi_format_link"
        );

        final StringFilterGroup videoTitle = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS_PLAYER_VIDEO_TITLE,
                "shorts_video_title_item"
        );

        pathFilterGroupList.addAll(
                shortsCompactFeedVideoPath,
                joinButton,
                reelSoundMetadata,
                subscribeButton,
                infoPanel,
                videoActionButton,
                videoLinkLabel,
                videoTitle
        );

        final ByteArrayAsStringFilterGroup shortsDislikeButton =
                new ByteArrayAsStringFilterGroup(
                        SettingsEnum.HIDE_SHORTS_PLAYER_DISLIKE_BUTTON,
                        "reel_dislike_button",
                        "reel_dislike_toggled_button"
                );

        final ByteArrayAsStringFilterGroup shortsLikeButton =
                new ByteArrayAsStringFilterGroup(
                        SettingsEnum.HIDE_SHORTS_PLAYER_LIKE_BUTTON,
                        "reel_like_button",
                        "reel_like_toggled_button"
                );

        final ByteArrayAsStringFilterGroup shortsCommentButton =
                new ByteArrayAsStringFilterGroup(
                        SettingsEnum.HIDE_SHORTS_PLAYER_COMMENTS_BUTTON,
                        "reel_comment_button"
                );

        final ByteArrayAsStringFilterGroup shortsRemixButton =
                new ByteArrayAsStringFilterGroup(
                        SettingsEnum.HIDE_SHORTS_PLAYER_REMIX_BUTTON,
                        "reel_remix_button"
                );

        final ByteArrayAsStringFilterGroup shortsShareButton =
                new ByteArrayAsStringFilterGroup(
                        SettingsEnum.HIDE_SHORTS_PLAYER_SHARE_BUTTON,
                        "reel_share_button"
                );

        videoActionButtonGroupList.addAll(
                shortsCommentButton,
                shortsDislikeButton,
                shortsLikeButton,
                shortsRemixButton,
                shortsShareButton
        );
    }

    @Override
    boolean isFiltered(String path, @Nullable String identifier, String allValue, byte[] protobufBufferArray,
                       FilterGroupList matchedList, FilterGroup matchedGroup, int matchedIndex) {
        if (exceptions.matches(path))
            return false;

        if (matchedList == pathFilterGroupList) {
            if (matchedGroup == infoPanel) {
                // Always filter if matched.
                return super.isFiltered(path, identifier, allValue, protobufBufferArray, matchedList, matchedGroup, matchedIndex);
            } else if (matchedGroup == shortsCompactFeedVideoPath) {
                if (matchedIndex == 0 && shortsCompactFeedVideoBuffer.check(protobufBufferArray).isFiltered())
                    return super.isFiltered(path, identifier, allValue, protobufBufferArray, matchedList, matchedGroup, matchedIndex);
                return false;
            } else if (matchedGroup == videoActionButton) {
                // Video action buttons have the same path.
                return videoActionButtonGroupList.check(protobufBufferArray).isFiltered();
            } else {
                // Filter other path groups from pathFilterGroupList, only when reelChannelBar is visible
                // to avoid false positives.
                return path.startsWith(REEL_CHANNEL_BAR_PATH);
            }
        } else if (matchedGroup == shelfHeader) {
            // Check ConversationContext to not hide shelf header in channel profile
            // This value does not exist in the shelf header in the channel profile
            return allValue.contains(SHORTS_SHELF_HEADER_CONVERSION_CONTEXT);
        }

        // Super class handles logging.
        return super.isFiltered(path, identifier, allValue, protobufBufferArray, matchedList, matchedGroup, matchedIndex);
    }
}