package com.sap.sailing.gwt.ui.raceboard;

import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.RaceTimePanelLifecycle;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.media.MediaPlayerLifecycle;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorRaceChartLifecycle;
import com.sap.sailing.gwt.ui.client.shared.charts.WindChartLifecycle;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapLifecycle;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanelLifecycle;
import com.sap.sse.gwt.client.shared.components.CompositeLifecycleSettings;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.AbstractPerspectiveLifecycle;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeLifecycleSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeLifecycleTabbedSettingsDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycleAndSettings;


public class RaceBoardPerspectiveLifecycle extends AbstractPerspectiveLifecycle<RaceBoardPerspectiveSettings,
    PerspectiveCompositeLifecycleSettings<RaceBoardPerspectiveLifecycle, RaceBoardPerspectiveSettings>,
    PerspectiveCompositeLifecycleTabbedSettingsDialogComponent<RaceBoardPerspectiveLifecycle, RaceBoardPerspectiveSettings>> {

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
    public PerspectiveCompositeLifecycleTabbedSettingsDialogComponent<RaceBoardPerspectiveLifecycle, RaceBoardPerspectiveSettings> getSettingsDialogComponent(
            PerspectiveCompositeLifecycleSettings<RaceBoardPerspectiveLifecycle, RaceBoardPerspectiveSettings> settings) {
        // collect the component lifecycle's for contained components and combine them with the corresponding settings
        
        // TOOD: Take the settings from the settings parameter
        CompositeLifecycleSettings componentLifecyclesAndDefaultSettings = getComponentLifecyclesAndDefaultSettings();
        
        RaceBoardPerspectiveSettings perspectiveSettings = settings.getPerspectiveLifecycleAndSettings().getSettings();
        PerspectiveLifecycleAndSettings<RaceBoardPerspectiveLifecycle, RaceBoardPerspectiveSettings> perspectiveLifecycleAndSettings =
                new PerspectiveLifecycleAndSettings<>(this, perspectiveSettings); 
        
        PerspectiveCompositeLifecycleSettings<RaceBoardPerspectiveLifecycle, RaceBoardPerspectiveSettings> perspectiveCompositeSettings = new PerspectiveCompositeLifecycleSettings<>(perspectiveLifecycleAndSettings, componentLifecyclesAndDefaultSettings);
        
        return new PerspectiveCompositeLifecycleTabbedSettingsDialogComponent<RaceBoardPerspectiveLifecycle, RaceBoardPerspectiveSettings>(perspectiveCompositeSettings);
    }

    @Override
    public PerspectiveCompositeLifecycleSettings<RaceBoardPerspectiveLifecycle, RaceBoardPerspectiveSettings> createDefaultSettings() {
        PerspectiveLifecycleAndSettings<RaceBoardPerspectiveLifecycle, RaceBoardPerspectiveSettings> perspectiveLifecycleAndSettings = 
                new PerspectiveLifecycleAndSettings<>(this, createPerspectiveDefaultSettings());  
        return new PerspectiveCompositeLifecycleSettings<>(perspectiveLifecycleAndSettings, getComponentLifecyclesAndDefaultSettings());
    }

    @Override
    public PerspectiveCompositeLifecycleSettings<RaceBoardPerspectiveLifecycle, RaceBoardPerspectiveSettings> cloneSettings(
            PerspectiveCompositeLifecycleSettings<RaceBoardPerspectiveLifecycle, RaceBoardPerspectiveSettings> settings) {
        throw new UnsupportedOperationException("Method not implemented yet.");
    }

    @Override
    public RaceBoardPerspectiveSettings createPerspectiveDefaultSettings() {
        return new RaceBoardPerspectiveSettings();
    }

    @Override
    public SettingsDialogComponent<RaceBoardPerspectiveSettings> getPerspectiveSettingsDialogComponent(RaceBoardPerspectiveSettings perspectiveSettings) {
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
