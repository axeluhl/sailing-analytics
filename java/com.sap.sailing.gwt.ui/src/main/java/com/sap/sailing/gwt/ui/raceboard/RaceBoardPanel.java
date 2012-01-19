package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.gwt.ui.adminconsole.RaceMap;
import com.sap.sailing.gwt.ui.adminconsole.RaceMapSettings;
import com.sap.sailing.gwt.ui.adminconsole.TimePanel;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RaceDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.components.Component;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;

public class RaceBoardPanel extends FormPanel implements Component<RaceBoardSettings> {

    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final Triple<EventDTO, RegattaDTO, RaceDTO> selectedRace;
    private String raceBoardName;

    private final Timer timer;
    private final List<CollapsableComponentViewer<?>> collapsableViewers;
    
    public RaceBoardPanel(SailingServiceAsync sailingService, final Triple<EventDTO, RegattaDTO, RaceDTO> theSelectedRace, String leaderboardName, 
            ErrorReporter errorReporter, final StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.selectedRace = theSelectedRace;
        this.setRaceBoardName(selectedRace.getC().name);
        this.errorReporter = errorReporter;
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setSize("100%", "100%");
        setWidget(mainPanel);

        timer = new Timer(/* delayBetweenAutoAdvancesInMilliseconds */500);
        collapsableViewers = new ArrayList<CollapsableComponentViewer<?>>();
        CompetitorSelectionModel competitorSelectionModel = new CompetitorSelectionModel(/* hasMultiSelection */ true);

        // create the default leaderboard
        LeaderboardPanel leaderboardPanel = new LeaderboardPanel(sailingService, competitorSelectionModel,
                leaderboardName, errorReporter, stringMessages);

        CollapsableComponentViewer<LeaderboardSettings> leaderboardViewer = new CollapsableComponentViewer<LeaderboardSettings>(
                leaderboardPanel, "100%", "100%", stringMessages);
        collapsableViewers.add(leaderboardViewer);

        // create the race map
        RaceMap raceMap = new RaceMap(sailingService, errorReporter, timer, competitorSelectionModel, stringMessages);
        CollapsableComponentViewer<RaceMapSettings> raceMapViewer = new CollapsableComponentViewer<RaceMapSettings>(
                raceMap, "600px", "300px", stringMessages);
        raceMap.loadMapsAPI((Panel) raceMapViewer.getViewerWidget().getContent());
        List<Triple<EventDTO, RegattaDTO, RaceDTO>> races = new ArrayList<Triple<EventDTO, RegattaDTO, RaceDTO>>();
        races.add(selectedRace);
        raceMap.onRaceSelectionChange(races);
        collapsableViewers.add(raceMapViewer);
        
        // create some sample components
        for(int i = 0; i < 2; i++) {
            SimpleComponentGroup<Object> componentGroup = new SimpleComponentGroup<Object>("Component Group " + i);
            componentGroup.addComponent(new SimpleComponent("My Component"));
            componentGroup.addComponent(new SimpleComponent("My Component 2"));
            componentGroup.addComponent(new SimpleComponent("My Component 3"));

            collapsableViewers.add(new CollapsableComponentViewer<Object>(componentGroup, "100%", "100px", stringMessages));
        }

        for (CollapsableComponentViewer<?> componentViewer : collapsableViewers) {
            mainPanel.add(componentViewer.getViewerWidget());
        }

        timer.addTimeListener(leaderboardPanel);
        timer.addTimeListener(raceMap);

        TimePanel timePanel = new TimePanel(stringMessages, timer);
        RaceDTO race = selectedRace.getC();
        if (race.startOfRace != null) {
            timePanel.timeChanged(race.startOfRace);
            timer.setTime(race.startOfRace.getTime());
        }
        if (race.startOfTracking != null) {
            timePanel.setMin(race.startOfTracking);
        }
        if (race.timePointOfNewestEvent != null) {
            timePanel.setMax(race.timePointOfNewestEvent);
        }
        
        mainPanel.add(timePanel);
    }
    
    @Override
    public Widget getEntryWidget() {
        return this;
    }

    public void updateSettings(RaceBoardSettings result) {

    }

    protected SailingServiceAsync getSailingService() {
        return sailingService;
    }

    protected String getRaceBoardName() {
        return raceBoardName;
    }

    protected void setRaceBoardName(String raceBoardName) {
        this.raceBoardName = raceBoardName;
    }

    protected ErrorReporter getErrorReporter() {
        return errorReporter;
    }

    @Override
    public String getLocalizedShortName() {
        return null;
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public SettingsDialogComponent<RaceBoardSettings> getSettingsDialogComponent() {
        return null;
    }
}

