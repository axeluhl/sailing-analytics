package com.sap.sailing.gwt.autoplay.client.shared.leaderboard;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.autoplay.client.shared.header.SAPHeaderComponentLifecycle;
import com.sap.sailing.gwt.settings.client.leaderboard.MultiRaceLeaderboardPanelLifecycle;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.AbstractPerspectiveLifecycle;

/**
 * A special stand alone Leaderboard with an SAP Header is handled by this lifecycle
 *
 */
public class LeaderboardWithZoomingPerspectiveLifecycle extends AbstractPerspectiveLifecycle<LeaderboardWithZoomingPerspectiveSettings> {
    
    private final SAPHeaderComponentLifecycle sapHeaderLifecycle;
    private final MultiRaceLeaderboardPanelLifecycle leaderboardPanelLifecycle;
    private final StringMessages stringMessages;
    
    public static final String ID = "lbwh";

    public LeaderboardWithZoomingPerspectiveLifecycle(AbstractLeaderboardDTO leaderboard, StringMessages stringMessages, Iterable<DetailType> availableDetailTypes) {
        this.stringMessages = stringMessages;
        this.leaderboardPanelLifecycle = new MultiRaceLeaderboardPanelLifecycle(leaderboard, stringMessages, availableDetailTypes);
        this.sapHeaderLifecycle = new SAPHeaderComponentLifecycle(stringMessages.leaderboard() +  ": " +
                (leaderboard.getDisplayName() == null ? leaderboard.getName() : leaderboard.getDisplayName()),
                        stringMessages);
        
        addLifeCycle(leaderboardPanelLifecycle);
        addLifeCycle(sapHeaderLifecycle);
    }
    
    @Override
    public LeaderboardWithZoomingPerspectiveSettings createPerspectiveOwnDefaultSettings() {
        return new LeaderboardWithZoomingPerspectiveSettings();
    }

    @Override
    public SettingsDialogComponent<LeaderboardWithZoomingPerspectiveSettings> getPerspectiveOwnSettingsDialogComponent(LeaderboardWithZoomingPerspectiveSettings perspectiveSettings) {
        return new LeaderboardPerspectiveSettingsDialogComponent(perspectiveSettings, stringMessages);
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.leaderboard() + " " + stringMessages.page();
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    public SAPHeaderComponentLifecycle getSapHeaderLifecycle() {
        return sapHeaderLifecycle;
    }

    public MultiRaceLeaderboardPanelLifecycle getLeaderboardPanelLifecycle() {
        return leaderboardPanelLifecycle;
    }

    @Override
    public String getComponentId() {
        return ID;
    }

    @Override
    protected LeaderboardWithZoomingPerspectiveSettings extractOwnUserSettings(
            LeaderboardWithZoomingPerspectiveSettings settings) {
        return settings;
    }

    @Override
    protected LeaderboardWithZoomingPerspectiveSettings extractOwnDocumentSettings(
            LeaderboardWithZoomingPerspectiveSettings settings) {
        return settings;
    }
}
