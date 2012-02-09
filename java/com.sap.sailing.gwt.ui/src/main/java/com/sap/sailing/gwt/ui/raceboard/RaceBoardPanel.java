package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.adminconsole.RaceMap;
import com.sap.sailing.gwt.ui.adminconsole.RaceMapSettings;
import com.sap.sailing.gwt.ui.adminconsole.WindChart;
import com.sap.sailing.gwt.ui.adminconsole.WindChartSettings;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.EventDisplayer;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TimePanel;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.client.UserAgentChecker.UserAgentTypes;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettingsFactory;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RaceDTO;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.UserDTO;
import com.sap.sailing.gwt.ui.shared.components.ComponentViewer;
import com.sap.sailing.gwt.ui.shared.panels.BreadcrumbPanel;

/**
 * A panel showing a list of components visualizing a race from the events announced by calls to {@link #fillEvents(List)}.
 * The race selection is provided by a {@link RaceSelectionProvider} for which this is a {@link RaceSelectionChangeListener listener}.
 * {@link RaceIdentifier}-based race selection changes are converted to {@link RaceDTO} objects using the {@link #racesByIdentifier}
 * map maintained during {@link #fillEvents(List)}. The race selection provider is expected to be single selection only.
 * 
 * @author Frank Mittag, Axel Uhl (d043530)
 *
 */
public class RaceBoardPanel extends FormPanel implements EventDisplayer, RaceSelectionChangeListener {
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private String raceBoardName;
    
    /**
     * Updated upon each {@link #fillEvents(List)}
     */
    private final Map<RaceIdentifier, RaceDTO> racesByIdentifier;

    private final List<CollapsableComponentViewer<?>> collapsableViewers;
    private final FlowPanel componentsNavigationPanel;
    private final BreadcrumbPanel breadcrumbPanel; 
    private final TimePanel timePanel;
    private final Timer timer;
    private final RaceSelectionProvider raceSelectionProvider;
    private final UserDTO user;
    
    public RaceBoardPanel(SailingServiceAsync sailingService, UserDTO currentUser, RaceSelectionProvider raceSelectionProvider, String leaderboardName,
            String leaderboardGroupName, ErrorReporter errorReporter, final StringMessages stringMessages, UserAgentTypes userAgentType) {
        this.sailingService = sailingService;
        this.user = currentUser;
        this.raceSelectionProvider = raceSelectionProvider;
        raceSelectionProvider.addRaceSelectionChangeListener(this);
        racesByIdentifier = new HashMap<RaceIdentifier, RaceDTO>();
        RaceIdentifier selectedRaceIdentifier = raceSelectionProvider.getSelectedRaces().iterator().next();
        this.setRaceBoardName(selectedRaceIdentifier.getRaceName());
        this.errorReporter = errorReporter;
        FlowPanel mainPanel = new FlowPanel();
        mainPanel.setSize("100%", "100%");
        setWidget(mainPanel);

        timer = new Timer(PlayModes.Replay, /* delayBetweenAutoAdvancesInMilliseconds */1000);
        collapsableViewers = new ArrayList<CollapsableComponentViewer<?>>();
        CompetitorSelectionModel competitorSelectionModel = new CompetitorSelectionModel(/* hasMultiSelection */ true);

        // create the breadcrumb navigation
        ArrayList<Pair<String, String>> breadcrumbLinksData = new ArrayList<Pair<String, String>>();
        String debugParam = Window.Location.getParameter("gwt.codesvr");

        if(leaderboardGroupName != null) {
            String link = "/gwt/Spectator.html?leaderboardGroupName=" + leaderboardGroupName;
            if(debugParam != null && !debugParam.isEmpty())
                link += "&gwt.codesvr=" + debugParam;
            breadcrumbLinksData.add(new Pair<String, String>(link, leaderboardGroupName));
        }
        breadcrumbPanel = new BreadcrumbPanel(breadcrumbLinksData, selectedRaceIdentifier.getRaceName());
        mainPanel.add(breadcrumbPanel);

        componentsNavigationPanel = new FlowPanel();
        componentsNavigationPanel.addStyleName("raceBoardNavigation");

        // create the default leaderboard and select the right race
        LeaderboardSettings leaderBoardSettings = LeaderboardSettingsFactory.getSettingsForUserRole(user);
        LeaderboardPanel leaderboardPanel = new LeaderboardPanel(sailingService, leaderBoardSettings, selectedRaceIdentifier, competitorSelectionModel,
                timer, leaderboardName, leaderboardGroupName, errorReporter, stringMessages, userAgentType);

        CollapsableComponentViewer<LeaderboardSettings> leaderboardViewer = new CollapsableComponentViewer<LeaderboardSettings>(
                leaderboardPanel, "100%", "100%", stringMessages);
        collapsableViewers.add(leaderboardViewer);

        // create the race map
        RaceMap raceMap = new RaceMap(sailingService, errorReporter, timer, competitorSelectionModel, stringMessages);
        CollapsableComponentViewer<RaceMapSettings> raceMapViewer = new CollapsableComponentViewer<RaceMapSettings>(
                raceMap, "auto", "500px", stringMessages);

        raceMap.loadMapsAPI((Panel) raceMapViewer.getViewerWidget().getContent());
        raceMap.onRaceSelectionChange(Collections.singletonList(selectedRaceIdentifier));
        collapsableViewers.add(raceMapViewer);
        
        boolean showWindChart = true;
        if(showWindChart) {
            WindChartSettings windChartSettings = new WindChartSettings(WindSource.values());
            WindChart windChart = new WindChart(sailingService, raceSelectionProvider, timer, windChartSettings, stringMessages, errorReporter); 
            CollapsableComponentViewer<WindChartSettings> windChartViewer = new CollapsableComponentViewer<WindChartSettings>(
                    windChart, "600px", "500px", stringMessages);
            windChart.onRaceSelectionChange(raceSelectionProvider.getSelectedRaces());
            collapsableViewers.add(windChartViewer);
        }

        for (CollapsableComponentViewer<?> componentViewer : collapsableViewers) {
            mainPanel.add(componentViewer.getViewerWidget());
            addComponentViewerMenuEntry(componentViewer);
        }

        timePanel = new TimePanel(timer, stringMessages);
    }

    
    private void addComponentViewerMenuEntry(final ComponentViewer c) {
        Anchor menuEntry = new Anchor(c.getViewerName());
        menuEntry.addStyleName("raceBoardNavigation-navigationitem");
        
        menuEntry.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                c.getViewerWidget().getElement().scrollIntoView();
            }
        });
        componentsNavigationPanel.add(menuEntry);
    }

    public Widget getNavigationWidget() {
        return componentsNavigationPanel; 
    }

    public Widget getBreadcrumbWidget() {
        return breadcrumbPanel; 
    }

    public Widget getTimeWidget() {
        return timePanel; 
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
    public void fillEvents(List<EventDTO> events) {
        racesByIdentifier.clear();
        for (EventDTO event : events) {
            for (RegattaDTO regatta : event.regattas) {
                for (RaceDTO race : regatta.races) {
                    if (race != null && race.getRaceIdentifier() != null) {
                        racesByIdentifier.put(race.getRaceIdentifier(), race);
                    }
                }
            }
        }
        // trigger selection change because now racesByIdentifier may have the information required, e.g., to update the time slider
        onRaceSelectionChange(raceSelectionProvider.getSelectedRaces());
    }

    @Override
    public void onRaceSelectionChange(List<RaceIdentifier> selectedRaces) {
        if (selectedRaces != null && !selectedRaces.isEmpty()) {
            RaceDTO selectedRace = racesByIdentifier.get(selectedRaces.iterator().next());
            
            Date min = null;
            Date max = null;
            
            if (selectedRace.startOfTracking != null) {
                min = selectedRace.startOfTracking;
            }
            if (selectedRace.endOfRace != null) {
                max = selectedRace.endOfRace;
            } else if (selectedRace.timePointOfNewestEvent != null) {
                max = selectedRace.timePointOfNewestEvent;
                timer.setPlayMode(PlayModes.Live);
            }

            if(min != null && max != null)
                timePanel.setMinMax(min, max);
            
            // set initial timer position
            switch(timer.getPlayMode()) {
                case Live:
                case Replay:
                    if(selectedRace.startOfRace != null) {
                        timer.setTime(selectedRace.startOfRace.getTime());
                    }
                    break;
            }
            sailingService.getRaceTimesInfo(selectedRace.getRaceIdentifier(), 
                    new AsyncCallback<RaceTimesInfoDTO>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError("Error obtaining leg timepoints: " + caught.getMessage());
                        }

                        @Override
                        public void onSuccess(RaceTimesInfoDTO racesTimesInfo) {
                            timePanel.setLegMarkers(racesTimesInfo.getLegTimes());
                            if (racesTimesInfo.getStartOfRace() != null) {
                                // set the new start time 
                                Date startOfRace = racesTimesInfo.getStartOfRace();
                                Date startOfTimeslider = new Date(startOfRace.getTime() - 5 * 60 * 1000);

                                timePanel.changeMin(startOfTimeslider);
                                timer.setTime(racesTimesInfo.getStartOfRace().getTime());
                            }
                        }
                    });
        }
    }
}

