package app.revanced.integrations.youtube.patches.components;

import androidx.annotation.Nullable;

import app.revanced.integrations.youtube.settings.SettingsEnum;

/**
 * @noinspection rawtypes
 */
@SuppressWarnings("unused")
final class QuickActionFilter extends Filter {
    private static final String QUICK_ACTION_PATH = "quick_actions.eml";
    private final StringFilterGroup quickActionRule;

    private final StringFilterGroup bufferFilterPathRule;
    private final ByteArrayFilterGroupList bufferButtonsGroupList = new ByteArrayFilterGroupList();

    public QuickActionFilter() {
        quickActionRule = new StringFilterGroup(null, QUICK_ACTION_PATH);
        identifierFilterGroupList.addAll(quickActionRule);
        bufferFilterPathRule = new StringFilterGroup(null, "button.eml|");

        pathFilterGroupList.addAll(
                new StringFilterGroup(
                        SettingsEnum.HIDE_QUICK_ACTIONS_LIKE_BUTTON,
                        "|like_button"
                ),
                new StringFilterGroup(
                        SettingsEnum.HIDE_QUICK_ACTIONS_DISLIKE_BUTTON,
                        "dislike_button"
                ),
                new StringFilterGroup(
                        SettingsEnum.HIDE_QUICK_ACTIONS_COMMENT_BUTTON,
                        "comments_entry_point_button"
                ),
                new StringFilterGroup(
                        SettingsEnum.HIDE_QUICK_ACTIONS_SAVE_TO_PLAYLIST_BUTTON,
                        "|save_to_playlist_button"
                ),
                new StringFilterGroup(
                        SettingsEnum.HIDE_QUICK_ACTIONS_MORE_BUTTON,
                        "|overflow_menu_button"
                ),
                new StringFilterGroup(
                        SettingsEnum.HIDE_QUICK_ACTIONS_RELATED_VIDEO,
                        "fullscreen_related_videos"
                ),
                bufferFilterPathRule
        );

        bufferButtonsGroupList.addAll(
                new ByteArrayAsStringFilterGroup(
                        SettingsEnum.HIDE_QUICK_ACTIONS_COMMENT_BUTTON,
                        "yt_outline_message_bubble_right"
                ),
                new ByteArrayAsStringFilterGroup(
                        SettingsEnum.HIDE_QUICK_ACTIONS_LIVE_CHAT_BUTTON,
                        "yt_outline_message_bubble_overlap"
                ),
                new ByteArrayAsStringFilterGroup(
                        SettingsEnum.HIDE_QUICK_ACTIONS_OPEN_MIX_PLAYLIST_BUTTON,
                        "yt_outline_youtube_mix"
                ),
                new ByteArrayAsStringFilterGroup(
                        SettingsEnum.HIDE_QUICK_ACTIONS_OPEN_PLAYLIST_BUTTON,
                        "yt_outline_list_play_arrow"
                ),
                new ByteArrayAsStringFilterGroup(
                        SettingsEnum.HIDE_QUICK_ACTIONS_SHARE_BUTTON,
                        "yt_outline_share"
                )
        );
    }

    private boolean isEveryFilterGroupEnabled() {
        for (StringFilterGroup group : pathFilterGroupList)
            if (!group.isEnabled()) return false;

        for (ByteArrayFilterGroup group : bufferButtonsGroupList)
            if (!group.isEnabled()) return false;

        return true;
    }

    @Override
    boolean isFiltered(String path, @Nullable String identifier, String allValue, byte[] protobufBufferArray,
                       FilterGroupList matchedList, FilterGroup matchedGroup, int matchedIndex) {
        if (!path.startsWith(QUICK_ACTION_PATH)) {
            return false;
        }
        if (matchedGroup == quickActionRule && !isEveryFilterGroupEnabled()) {
            return false;
        }
        if (matchedGroup == bufferFilterPathRule) {
            return bufferButtonsGroupList.check(protobufBufferArray).isFiltered();
        }

        return super.isFiltered(path, identifier, allValue, protobufBufferArray, matchedList, matchedGroup, matchedIndex);
    }
}
