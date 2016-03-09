package com.sap.sailing.gwt.autoplay.client.shared.leaderboard;

import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.autoplay.client.shared.header.SAPHeaderComponentLifecycle;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanelLifecycle;
import com.sap.sse.gwt.client.shared.perspective.AbstractPerspectiveLifecycle;

public class LeaderboardWithHeaderPerspectiveLifecycle extends AbstractPerspectiveLifecycle<LeaderboardWithHeaderPerspective, 
    LeaderboardWithHeaderPerspectiveSettings, LeaderboardPerspectiveSettingsDialogComponent> {

    private final SAPHeaderComponentLifecycle sapHeaderLifecycle;
    private final LeaderboardPanelLifecycle leaderboardPanelLifecycle;
    private final StringMessages stringMessages;

    public LeaderboardWithHeaderPerspectiveLifecycle(AbstractLeaderboardDTO leaderboard, StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
        this.leaderboardPanelLifecycle = new LeaderboardPanelLifecycle(leaderboard, stringMessages);
        this.sapHeaderLifecycle = new SAPHeaderComponentLifecycle(stringMessages.leaderboard() +  ": " + leaderboard.getDisplayName(), stringMessages);
        
        this.componentLifecycles.add(leaderboardPanelLifecycle);
        this.componentLifecycles.add(sapHeaderLifecycle);
    }
    
    @Override
    public LeaderboardPerspectiveSettingsDialogComponent getSettingsDialogComponent(LeaderboardWithHeaderPerspectiveSettings settings) {
        return new LeaderboardPerspectiveSettingsDialogComponent(settings, stringMessages);
    }

    @Override
    public LeaderboardWithHeaderPerspectiveSettings createDefaultSettings() {
        return new LeaderboardWithHeaderPerspectiveSettings();
    }

    @Override
    public LeaderboardWithHeaderPerspectiveSettings cloneSettings(LeaderboardWithHeaderPerspectiveSettings settings) {
        return new LeaderboardWithHeaderPerspectiveSettings(settings.isLeaderboardAutoZoom(), settings.getLeaderboardZoomFactor());
    }

    @Override
    public String getLocalizedShortName() {
        return "Leaderboard Viewer";
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    public SAPHeaderComponentLifecycle getSapHeaderLifecycle() {
        return sapHeaderLifecycle;
    }

    public LeaderboardPanelLifecycle getLeaderboardPanelLifecycle() {
        return leaderboardPanelLifecycle;
    }

}
