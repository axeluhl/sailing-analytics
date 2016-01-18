package com.sap.sailing.gwt.autoplay.client.place.player;

import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.media.MediaPlayerLifecycle;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorRaceChartLifecycle;
import com.sap.sailing.gwt.ui.client.shared.charts.WindChartLifecycle;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapLifecycle;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanelLifecycle;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPanel;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveSettings;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveSettingsDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.AbstractPerspectiveLifecycle;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveConstructorArgs;

public class RaceBoardPerspectiveLifecycle extends AbstractPerspectiveLifecycle<RaceBoardPanel, 
    RaceBoardPerspectiveSettings, RaceBoardPerspectiveSettingsDialogComponent,
    RaceBoardPerspectiveLifecycle.RaceBoardPerspectiveConstructorArgs> {

    private final StringMessages stringMessages;
    
    public RaceBoardPerspectiveLifecycle(AbstractLeaderboardDTO leaderboard, StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        
        componentLifecycles.add(new RaceMapLifecycle(stringMessages));
        componentLifecycles.add(new WindChartLifecycle(stringMessages));
        componentLifecycles.add(new LeaderboardPanelLifecycle(leaderboard, stringMessages));
        componentLifecycles.add(new MultiCompetitorRaceChartLifecycle(stringMessages));
        componentLifecycles.add(new MediaPlayerLifecycle(stringMessages));
    }
    
    @Override
    public RaceBoardPerspectiveSettingsDialogComponent getSettingsDialogComponent(RaceBoardPerspectiveSettings settings) {
        return new RaceBoardPerspectiveSettingsDialogComponent(settings, stringMessages);
    }

    @Override
    public RaceBoardPerspectiveSettings createDefaultSettings() {
        return new RaceBoardPerspectiveSettings();
    }

    @Override
    public RaceBoardPerspectiveSettings cloneSettings(RaceBoardPerspectiveSettings settings) {
        return new RaceBoardPerspectiveSettings(settings.getActiveCompetitorsFilterSetName(), settings.isShowLeaderboard(),
                settings.isShowWindChart(), settings.isShowCompetitorsChart(), settings.isShowViewStreamlets(),
                settings.isShowViewStreamletColors(), settings.isShowViewSimulation(), settings.isCanReplayDuringLiveRaces());
    }

    @Override
    public String getLocalizedShortName() {
        return "Race Viewer";
    }

    @Override
    public boolean hasSettings() {
        return true;
    }
    
    @Override
    public RaceBoardPanel createComponent(RaceBoardPerspectiveConstructorArgs raceboardPerspectiveConstructorArgs,
            RaceBoardPerspectiveSettings settings) {
        return null;
    }

    public class RaceBoardPerspectiveConstructorArgs implements PerspectiveConstructorArgs<RaceBoardPanel, RaceBoardPerspectiveSettings> {
        public RaceBoardPerspectiveConstructorArgs() {
        }
        
        @Override
        public RaceBoardPanel createComponent(RaceBoardPerspectiveSettings newSettings) {
            return null;
        }
    }

}
