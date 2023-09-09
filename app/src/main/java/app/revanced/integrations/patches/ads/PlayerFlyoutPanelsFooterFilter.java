package app.revanced.integrations.patches.ads;

import androidx.annotation.Nullable;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.StringTrieSearch;

final class PlayerFlyoutPanelsFooterFilter extends Filter {
    private final StringTrieSearch exceptions = new StringTrieSearch();

    public PlayerFlyoutPanelsFooterFilter() {
        exceptions.addPatterns(
            "bottom_sheet_list_option"
        );

        pathFilterGroups.addAll(
            new StringFilterGroup(
                SettingsEnum.HIDE_PLAYER_FLYOUT_PANEL_QUALITY_FOOTER,
                "quality_sheet_footer",
                "|divider.eml|"
            ),
            new StringFilterGroup(
                SettingsEnum.HIDE_PLAYER_FLYOUT_PANEL_CAPTIONS_FOOTER,
                "|ContainerType|ContainerType|ContainerType|TextType|",
                "|divider.eml|"
            )
        );
    }

    @Override
    boolean isFiltered(String path, @Nullable String identifier, String allValue, byte[] protobufBufferArray,
                       FilterGroupList matchedList, FilterGroup matchedGroup, int matchedIndex) {
        // If the flyout is not caption or quality, skip
        if (!path.contains("captions_sheet_content.eml-js") || !path.contains("advanced_quality_sheet_content.eml-js") || exceptions.matches(path))
            return false;

        return super.isFiltered(path, identifier, allValue, protobufBufferArray, matchedList, matchedGroup, matchedIndex);
    }
}
