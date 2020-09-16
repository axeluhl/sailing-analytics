package com.sap.sailing.gwt.settings.client.leaderboard;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public abstract class AbstractMultiRaceLeaderboardPanelLifecycle<T extends LeaderboardSettings> extends LeaderboardPanelLifecycle<T> {

    protected final List<String> namesOfRaceColumns;
    protected final boolean canBoatInfoBeShown;
    
    public AbstractMultiRaceLeaderboardPanelLifecycle(AbstractLeaderboardDTO leaderboard, StringMessages stringMessages,
            Iterable<DetailType> availableDetailTypes) {
        this(leaderboard != null ? leaderboard.getNamesOfRaceColumns() : new ArrayList<String>(),
                leaderboard != null ? !leaderboard.canBoatsOfCompetitorsChangePerRace : false, stringMessages,
                availableDetailTypes);
    }
    
    protected AbstractMultiRaceLeaderboardPanelLifecycle(List<String> namesOfRaceColumns, boolean canBoatInfoBeShown, StringMessages stringMessages,
            Iterable<DetailType> availableDetailTypes) {
        super(stringMessages, availableDetailTypes);
        this.namesOfRaceColumns = namesOfRaceColumns;
        this.canBoatInfoBeShown = canBoatInfoBeShown;
    }
    
    public abstract T extractUserSettings(T currentLeaderboardSettings);
    
    public abstract SettingsDialogComponent<T> getSettingsDialogComponent(T settings);

    public abstract T createDefaultSettings();
}
