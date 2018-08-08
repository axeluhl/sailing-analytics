package com.sap.sailing.gwt.settings.client.leaderboard;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class MultiRaceLeaderboardPanelLifecycle extends LeaderboardPanelLifecycle<MultiRaceLeaderboardSettings> {

    protected final List<String> namesOfRaceColumns;
    
    public MultiRaceLeaderboardPanelLifecycle(AbstractLeaderboardDTO leaderboard, StringMessages stringMessages,
            Iterable<DetailType> availableDetailTypes) {
        super(stringMessages, availableDetailTypes);
        this.namesOfRaceColumns = leaderboard != null ? leaderboard.getNamesOfRaceColumns() : new ArrayList<String>();
    }
    
    @Override
    public MultiRaceLeaderboardSettings extractUserSettings(MultiRaceLeaderboardSettings currentLeaderboardSettings) {
        // All settings except namesOfRaceColumnsToShow are used for the user settings
        return currentLeaderboardSettings.withDefaultNamesOfRaceColumnsToShow(namesOfRaceColumns);
    }
    
    @Override
    public MultiRaceLeaderboardSettingsDialogComponent getSettingsDialogComponent(MultiRaceLeaderboardSettings settings) {
        return new MultiRaceLeaderboardSettingsDialogComponent(settings, namesOfRaceColumns, stringMessages, availableDetailTypes, false);
    }

    @Override
    public MultiRaceLeaderboardSettings createDefaultSettings() {
        return LeaderboardSettingsFactory.getInstance()
                .createNewDefaultSettingsWithRaceColumns(namesOfRaceColumns);
    }
}
