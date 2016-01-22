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
    RaceBoardPerspectiveLifecycle.ConstructorArgs> {

    private final StringMessages stringMessages;
    private final RaceMapLifecycle raceMapLifecycle;
    private final WindChartLifecycle windChartLifecycle;
    private final LeaderboardPanelLifecycle leaderboardPanelLifecycle;
    private final MultiCompetitorRaceChartLifecycle multiCompetitorRaceChartLifecycle;
    private final MediaPlayerLifecycle mediaPlayerLifecycle;
    
    public RaceBoardPerspectiveLifecycle(AbstractLeaderboardDTO leaderboard, StringMessages stringMessages) {
        this.stringMessages = stringMessages;

        raceMapLifecycle = new RaceMapLifecycle(stringMessages);
        windChartLifecycle = new WindChartLifecycle(stringMessages);
        leaderboardPanelLifecycle = new LeaderboardPanelLifecycle(leaderboard, stringMessages);
        multiCompetitorRaceChartLifecycle = new MultiCompetitorRaceChartLifecycle(stringMessages);
        mediaPlayerLifecycle = new MediaPlayerLifecycle(stringMessages);
        
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
    
    public RaceMapLifecycle getRaceMapLifecycle() {
        return raceMapLifecycle;
    }

    public WindChartLifecycle getWindChartLifecycle() {
        return windChartLifecycle;
    }

    public LeaderboardPanelLifecycle getLeaderboardPanelLifecycle() {
        return leaderboardPanelLifecycle;
    }

    public MultiCompetitorRaceChartLifecycle getMultiCompetitorRaceChartLifecycle() {
        return multiCompetitorRaceChartLifecycle;
    }

    public MediaPlayerLifecycle getMediaPlayerLifecycle() {
        return mediaPlayerLifecycle;
    }
    
    @Override
    public RaceBoardPanel createComponent(ConstructorArgs raceboardPerspectiveConstructorArgs,
            RaceBoardPerspectiveSettings settings) {
        return raceboardPerspectiveConstructorArgs.createComponent(settings);
    }

    public static class ConstructorArgs implements PerspectiveConstructorArgs<RaceBoardPanel, RaceBoardPerspectiveSettings> {
        private final WindChartLifecycle.ConstructionParameters windChartConstParams;
        private final RaceMapLifecycle.ConstructionParameters raceMapConstParams;
        private final LeaderboardPanelLifecycle.ConstructionParameters leaderboardPanelConstParams;
        private final MultiCompetitorRaceChartLifecycle.ConstructionParameters multiChartConstParams;
        private final MediaPlayerLifecycle.ConstructionParameters mediaPlayerConstParams;

        public ConstructorArgs(WindChartLifecycle.ConstructionParameters windChartConstParams,
                RaceMapLifecycle.ConstructionParameters raceMapConstParams,
                LeaderboardPanelLifecycle.ConstructionParameters leaderboardPanelConstParams,
                MultiCompetitorRaceChartLifecycle.ConstructionParameters multiChartConstParams,
                MediaPlayerLifecycle.ConstructionParameters mediaPlayerConstParams) {
            this.windChartConstParams = windChartConstParams;
            this.raceMapConstParams = raceMapConstParams;
            this.leaderboardPanelConstParams = leaderboardPanelConstParams;
            this.multiChartConstParams = multiChartConstParams;
            this.mediaPlayerConstParams = mediaPlayerConstParams;
        }
        
        @Override
        public RaceBoardPanel createComponent(RaceBoardPerspectiveSettings newSettings) {
            return null;
        }
    }
}
