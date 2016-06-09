package com.sap.sailing.gwt.ui.raceboard;

import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.RaceTimePanelLifecycle;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.media.MediaPlayerLifecycle;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorRaceChartLifecycle;
import com.sap.sailing.gwt.ui.client.shared.charts.WindChartLifecycle;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapLifecycle;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanelLifecycle;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.AbstractPerspectiveLifecycle;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;


public class RaceBoardPerspectiveLifecycle extends AbstractPerspectiveLifecycle<RaceBoardPerspectiveSettings> {

    private final StringMessages stringMessages;
    private final RaceMapLifecycle raceMapLifecycle;
    private final WindChartLifecycle windChartLifecycle;
    private final LeaderboardPanelLifecycle leaderboardPanelLifecycle;
    private final MultiCompetitorRaceChartLifecycle multiCompetitorRaceChartLifecycle;
    private final MediaPlayerLifecycle mediaPlayerLifecycle;
    private final RaceTimePanelLifecycle raceTimePanelLifecycle; 

    public RaceBoardPerspectiveLifecycle(StringMessages stringMessages) {
        this(null, stringMessages);
    }
    
    public RaceBoardPerspectiveLifecycle(AbstractLeaderboardDTO leaderboard, StringMessages stringMessages) {
        this.stringMessages = stringMessages;

        raceMapLifecycle = new RaceMapLifecycle(stringMessages);
        windChartLifecycle = new WindChartLifecycle(stringMessages);
        leaderboardPanelLifecycle = new LeaderboardPanelLifecycle(leaderboard, stringMessages);
        multiCompetitorRaceChartLifecycle = new MultiCompetitorRaceChartLifecycle(stringMessages, false);
        mediaPlayerLifecycle = new MediaPlayerLifecycle(stringMessages);
        raceTimePanelLifecycle = new RaceTimePanelLifecycle(stringMessages);
        
        componentLifecycles.add(raceMapLifecycle);
        componentLifecycles.add(windChartLifecycle);
        componentLifecycles.add(leaderboardPanelLifecycle);
        componentLifecycles.add(multiCompetitorRaceChartLifecycle);
        componentLifecycles.add(mediaPlayerLifecycle);
        componentLifecycles.add(raceTimePanelLifecycle);
    }

    @Override
    public PerspectiveCompositeSettings<RaceBoardPerspectiveSettings> cloneSettings(
            PerspectiveCompositeSettings<RaceBoardPerspectiveSettings> settings) {
        throw new UnsupportedOperationException("Method not implemented yet.");
    }

    @Override
    public RaceBoardPerspectiveSettings createPerspectiveOwnDefaultSettings() {
        return new RaceBoardPerspectiveSettings();
    }

    @Override
    public SettingsDialogComponent<RaceBoardPerspectiveSettings> getPerspectiveOwnSettingsDialogComponent(RaceBoardPerspectiveSettings perspectiveSettings) {
        return new RaceBoardPerspectiveSettingsDialogComponent(perspectiveSettings, stringMessages);
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.raceViewer();
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

    public RaceTimePanelLifecycle getRaceTimePanelLifecycle() {
        return raceTimePanelLifecycle;
    }
}
