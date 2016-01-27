package com.sap.sailing.gwt.autoplay.client.place.player;

import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.autoplay.client.shared.header.SAPHeaderLifecycle;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanelLifecycle;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPerspectiveSettings;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPerspectiveSettingsDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.AbstractPerspectiveLifecycle;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveConstructorArgs;

public class LeaderboardWithHeaderPerspectiveLifecycle extends AbstractPerspectiveLifecycle<LeaderboardWithHeaderPerspective, 
    LeaderboardPerspectiveSettings, LeaderboardPerspectiveSettingsDialogComponent,
    LeaderboardWithHeaderPerspectiveLifecycle.ConstructorArgs> {

    private final SAPHeaderLifecycle sapHeaderLifecycle;
    private final LeaderboardPanelLifecycle leaderboardPanelLifecycle;
    private final StringMessages stringMessages;

    public LeaderboardWithHeaderPerspectiveLifecycle(AbstractLeaderboardDTO leaderboard,  StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
        this.leaderboardPanelLifecycle = new LeaderboardPanelLifecycle(leaderboard, stringMessages);
        this.sapHeaderLifecycle = new SAPHeaderLifecycle();
        
        this.componentLifecycles.add(leaderboardPanelLifecycle);
        this.componentLifecycles.add(sapHeaderLifecycle);
    }
    
    @Override
    public LeaderboardPerspectiveSettingsDialogComponent getSettingsDialogComponent(LeaderboardPerspectiveSettings settings) {
        return new LeaderboardPerspectiveSettingsDialogComponent(settings, stringMessages);
    }

    @Override
    public LeaderboardPerspectiveSettings createDefaultSettings() {
        return new LeaderboardPerspectiveSettings();
    }

    @Override
    public LeaderboardPerspectiveSettings cloneSettings(LeaderboardPerspectiveSettings settings) {
        return new LeaderboardPerspectiveSettings(settings.isLeaderboardAutoZoom(), settings.getLeaderboardZoomFactor());
    }

    @Override
    public String getLocalizedShortName() {
        return "Leaderboard Viewer";
    }

    @Override
    public boolean hasSettings() {
        return true;
    }


    public SAPHeaderLifecycle getSapHeaderLifecycle() {
        return sapHeaderLifecycle;
    }

    public LeaderboardPanelLifecycle getLeaderboardPanelLifecycle() {
        return leaderboardPanelLifecycle;
    }

    @Override
    public LeaderboardWithHeaderPerspective createComponent(ConstructorArgs leaderboardPerspectiveConstructorArgs,
            LeaderboardPerspectiveSettings settings) {
        return leaderboardPerspectiveConstructorArgs.createComponent(settings);
    }

    public static class ConstructorArgs implements PerspectiveConstructorArgs<LeaderboardWithHeaderPerspective, LeaderboardPerspectiveSettings> {
        private final SAPHeaderLifecycle.ConstructionParameters sapHeaderConstructionParameters;
        private final LeaderboardPanelLifecycle.ConstructionParameters leaderboardConstructionParameters;
        
        public ConstructorArgs(SAPHeaderLifecycle.ConstructionParameters sapHeaderConstructionParameters,
                LeaderboardPanelLifecycle.ConstructionParameters leaderboardConstructionParameters) {
            this.sapHeaderConstructionParameters = sapHeaderConstructionParameters;
            this.leaderboardConstructionParameters = leaderboardConstructionParameters;
        }
        
        @Override
        public LeaderboardWithHeaderPerspective createComponent(LeaderboardPerspectiveSettings settings) {
            return new LeaderboardWithHeaderPerspective(settings, sapHeaderConstructionParameters, leaderboardConstructionParameters, StringMessages.INSTANCE);
        }
    }
}
