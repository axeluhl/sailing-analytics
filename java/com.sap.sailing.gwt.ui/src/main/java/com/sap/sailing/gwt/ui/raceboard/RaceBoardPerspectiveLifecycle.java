package com.sap.sailing.gwt.ui.raceboard;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.settings.client.raceboard.RaceBoardPerspectiveOwnSettings;
import com.sap.sailing.gwt.ui.client.RaceTimePanelLifecycle;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.media.MediaPlayerLifecycle;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorRaceChartLifecycle;
import com.sap.sailing.gwt.ui.client.shared.charts.WindChartLifecycle;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapLifecycle;
import com.sap.sailing.gwt.ui.client.shared.racemap.maneuver.ManeuverTableLifecycle;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.AbstractPerspectiveLifecycle;
import com.sap.sse.security.ui.client.UserService;


public class RaceBoardPerspectiveLifecycle extends AbstractPerspectiveLifecycle<RaceBoardPerspectiveOwnSettings> {

    private final StringMessages stringMessages;
    private final RaceMapLifecycle raceMapLifecycle;
    private final WindChartLifecycle windChartLifecycle;
    private final SingleRaceLeaderboardPanelLifecycle leaderboardPanelLifecycle;
    private final MultiCompetitorRaceChartLifecycle multiCompetitorRaceChartLifecycle;
    private final MediaPlayerLifecycle mediaPlayerLifecycle;
    private final RaceTimePanelLifecycle raceTimePanelLifecycle;
    private final ManeuverTableLifecycle maneuverTableLifecycle;
    
    public static final String ID = "rb";
    
    public RaceBoardPerspectiveLifecycle(AbstractLeaderboardDTO leaderboard, StringMessages stringMessages,
            Iterable<DetailType> competitorChartAllowedDetailTypes, UserService userService,
            Iterable<DetailType> availableDetailTypes) {
        this.stringMessages = stringMessages;
        raceMapLifecycle = new RaceMapLifecycle(stringMessages);
        windChartLifecycle = new WindChartLifecycle(stringMessages);
        maneuverTableLifecycle = new ManeuverTableLifecycle(stringMessages);
        leaderboardPanelLifecycle = new SingleRaceLeaderboardPanelLifecycle(stringMessages, availableDetailTypes,
                leaderboard.canBoatsOfCompetitorsChangePerRace);
        multiCompetitorRaceChartLifecycle = new MultiCompetitorRaceChartLifecycle(stringMessages, competitorChartAllowedDetailTypes);
        mediaPlayerLifecycle = new MediaPlayerLifecycle(stringMessages);
        raceTimePanelLifecycle = new RaceTimePanelLifecycle(stringMessages, userService);
        
        addLifeCycle(raceMapLifecycle);
        addLifeCycle(windChartLifecycle);
        addLifeCycle(leaderboardPanelLifecycle);
        addLifeCycle(multiCompetitorRaceChartLifecycle);
        addLifeCycle(mediaPlayerLifecycle);
        addLifeCycle(raceTimePanelLifecycle);
        addLifeCycle(maneuverTableLifecycle);
    }

    @Override
    public RaceBoardPerspectiveOwnSettings createPerspectiveOwnDefaultSettings() {
        return new RaceBoardPerspectiveOwnSettings();
    }

    @Override
    public SettingsDialogComponent<RaceBoardPerspectiveOwnSettings> getPerspectiveOwnSettingsDialogComponent(RaceBoardPerspectiveOwnSettings perspectiveSettings) {
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
    
    public ManeuverTableLifecycle getManeuverTable() {
        return maneuverTableLifecycle;
    }
    
    public RaceMapLifecycle getRaceMapLifecycle() {
        return raceMapLifecycle;
    }

    public WindChartLifecycle getWindChartLifecycle() {
        return windChartLifecycle;
    }

    public SingleRaceLeaderboardPanelLifecycle getLeaderboardPanelLifecycle() {
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

    @Override
    public String getComponentId() {
        return ID;
    }

    @Override
    protected RaceBoardPerspectiveOwnSettings extractOwnUserSettings(RaceBoardPerspectiveOwnSettings settings) {
        return settings;
    }

    @Override
    protected RaceBoardPerspectiveOwnSettings extractOwnDocumentSettings(RaceBoardPerspectiveOwnSettings settings) {
        return settings;
    }
}
