package app.revanced.integrations.youtube.patches.overlaybutton;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import app.revanced.integrations.shared.utils.Logger;
import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.youtube.utils.VideoUtils;

@SuppressWarnings("unused")
public class PlayAll extends BottomControlButton {

    public enum PlaylistPrefixType {
        // https://github.com/RobertWesner/YouTube-Play-All/blob/main/documentation/available-lists.md
        ALL_CONTENTS_WITH_TIME_DESCENDING("UU"),
        ALL_CONTENTS_WITH_POPULAR_DESCENDING("PU"),
        VIDEOS_ONLY_WITH_TIME_DESCENDING("UULF"),
        VIDEOS_ONLY_WITH_POPULAR_DESCENDING("UULP"),
        SHORTS_ONLY_WITH_TIME_DESCENDING("UUSH"),
        SHORTS_ONLY_WITH_POPULAR_DESCENDING("UUPS"),
        LIVESTREAMS_ONLY_WITH_TIME_DESCENDING("UULV"),
        LIVESTREAMS_ONLY_WITH_POPULAR_DESCENDING("UUPV"),
        ALL_MEMBERSHIPS_CONTENTS("UUMO"),
        MEMBERSHIPS_VIDEOS_ONLY("UUMF"),
        MEMBERSHIPS_SHORTS_ONLY("UUMS"),
        MEMBERSHIPS_LIVESTREAMS_ONLY("UUMV");

        /**
         * Prefix of playlist id.
         */
        final String prefixId;

        PlaylistPrefixType(String prefixId) {
            this.prefixId = prefixId;
        }
    }

    @Nullable
    private static PlayAll instance;
    private static final PlaylistPrefixType CURRENT_TYPE = Settings.OVERLAY_BUTTON_PLAY_ALL_TYPE.get();

    public PlayAll(ViewGroup bottomControlsViewGroup) {
        super(
                bottomControlsViewGroup,
                "play_all_button",
                Settings.OVERLAY_BUTTON_PLAY_ALL,
                view -> VideoUtils.openVideo(true, CURRENT_TYPE.prefixId),
                view -> {
                    VideoUtils.openVideo(false, null);
                    return true;
                }
        );
    }

    /**
     * Injection point.
     */
    public static void initialize(View bottomControlsViewGroup) {
        try {
            if (bottomControlsViewGroup instanceof ViewGroup viewGroup) {
                instance = new PlayAll(viewGroup);
            }
        } catch (Exception ex) {
            Logger.printException(() -> "initialize failure", ex);
        }
    }

    /**
     * Injection point.
     */
    public static void changeVisibility(boolean showing, boolean animation) {
        if (instance != null) instance.setVisibility(showing, animation);
    }

    public static void changeVisibilityNegatedImmediate() {
        if (instance != null) instance.setVisibilityNegatedImmediate();
    }

}
