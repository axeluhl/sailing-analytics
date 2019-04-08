package com.sap.sailing.gwt.settings.client.leaderboard;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.settings.generic.support.SettingsUtil;

public class MultiRaceLeaderboardPanelLifecycle extends LeaderboardPanelLifecycle<MultiRaceLeaderboardSettings> {

    protected final List<String> namesOfRaceColumns;
    private final boolean canBoatInfoBeShown;
    
    public MultiRaceLeaderboardPanelLifecycle(AbstractLeaderboardDTO leaderboard, StringMessages stringMessages,
            Iterable<DetailType> availableDetailTypes) {
        this(leaderboard != null ? leaderboard.getNamesOfRaceColumns() : new ArrayList<String>(),
                leaderboard != null ? !leaderboard.canBoatsOfCompetitorsChangePerRace : false, stringMessages,
                availableDetailTypes);
    }
    
    protected MultiRaceLeaderboardPanelLifecycle(List<String> namesOfRaceColumns, boolean canBoatInfoBeShown, StringMessages stringMessages,
            Iterable<DetailType> availableDetailTypes) {
        super(stringMessages, availableDetailTypes);
        this.namesOfRaceColumns = namesOfRaceColumns;
        this.canBoatInfoBeShown = canBoatInfoBeShown;
    }
    
    @Override
    public MultiRaceLeaderboardSettings extractUserSettings(MultiRaceLeaderboardSettings currentLeaderboardSettings) {
        // All settings except namesOfRaceColumnsToShow are used for the user settings
        return currentLeaderboardSettings.withNamesOfRaceColumnsToShowDefaultsAndValues(namesOfRaceColumns);
    }
    
    @Override
    public MultiRaceLeaderboardSettingsDialogComponent getSettingsDialogComponent(MultiRaceLeaderboardSettings settings) {
        return new MultiRaceLeaderboardSettingsDialogComponent(settings, namesOfRaceColumns, stringMessages,
                availableDetailTypes, canBoatInfoBeShown);
    }

    @Override
    public MultiRaceLeaderboardSettings createDefaultSettings() {
        MultiRaceLeaderboardSettings leaderboardSettings = new MultiRaceLeaderboardSettings(namesOfRaceColumns);
        SettingsUtil.copyDefaultsFromValues(leaderboardSettings, leaderboardSettings);
        return leaderboardSettings;
    }
}
