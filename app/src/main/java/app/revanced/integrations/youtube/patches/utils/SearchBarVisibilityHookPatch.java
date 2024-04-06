package app.revanced.integrations.youtube.patches.utils;

import android.view.View;

import app.revanced.integrations.youtube.utils.LogHelper;

@SuppressWarnings("unused")
public final class SearchBarVisibilityHookPatch {
    private static volatile boolean searchbarIsActive;

    /**
     * Injection point.
     */
    public static void searchBarResultsViewLoaded(View searchbarResults) {
        searchbarResults.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            final boolean isActive = searchbarResults.getParent() != null;

            if (searchbarIsActive != isActive) {
                searchbarIsActive = isActive;
                LogHelper.printDebug(() -> "searchbarIsActive: " + isActive);
            }
        });
    }

    public static boolean isSearchBarActive() {
        return searchbarIsActive;
    }

}