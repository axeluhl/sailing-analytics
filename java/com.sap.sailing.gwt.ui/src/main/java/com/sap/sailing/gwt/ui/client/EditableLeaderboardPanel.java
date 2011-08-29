package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;

/**
 * An editable version of the {@link LeaderboardPanel} which allows a user to enter carried / accumulated
 * points and fix individual race scores.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class EditableLeaderboardPanel extends LeaderboardPanel {

    public EditableLeaderboardPanel(SailingServiceAsync sailingService, String leaderboardName,
            ErrorReporter errorReporter, StringConstants stringConstants) {
        super(sailingService, leaderboardName, errorReporter, stringConstants);
    }

    /**
     * Always ensures that there is a carry column displayed because in the editable version
     * of the leaderboard the carried / accumulated values must always be editable and therefore
     * the column must always be shown.
     */
    @Override
    protected void updateCarryColumn(LeaderboardDAO leaderboard) {
        ensureCarryColumn();
    }

}
