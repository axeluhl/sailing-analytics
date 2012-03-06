package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
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
import com.sap.sailing.gwt.ui.client.RaceTimePanel;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.client.UserAgentChecker.UserAgentTypes;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettingsFactory;
import com.sap.sailing.gwt.ui.leaderboard.MultiChartPanel;
import com.sap.sailing.gwt.ui.leaderboard.MultiChartSettings;
import com.sap.sailing.gwt.ui.raceboard.CollapsableComponentViewer.ViewerPanelTypes;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RaceDTO;
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
    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    private String raceBoardName;
    
    /**
     * Updated upon each {@link #fillEvents(List)}
     */
    private final Map<RaceIdentifier, RaceDTO> racesByIdentifier;
    
    /**
     * The offset when scrolling with the menu entry anchors (in the top right corner).
     */
    private int scrollOffset;

    private final List<ComponentViewer> componentViewers;
    private FlowPanel componentsNavigationPanel;
    private BreadcrumbPanel breadcrumbPanel; 
    private RaceTimePanel timePanel;
    private final Timer timer;
    private final RaceSelectionProvider raceSelectionProvider;
    private final UserAgentTypes userAgentType;
    private final CompetitorSelectionModel competitorSelectionModel;
    private final RaceIdentifier selectedRaceIdentifier;
    
    public RaceBoardPanel(SailingServiceAsync sailingService, UserDTO theUser, RaceSelectionProvider theRaceSelectionProvider, String leaderboardName,
            String leaderboardGroupName, ErrorReporter errorReporter, final StringMessages stringMessages, UserAgentTypes userAgentType, RaceBoardViewMode viewMode) {
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.raceSelectionProvider = theRaceSelectionProvider;
        this.scrollOffset = 0;
        raceSelectionProvider.addRaceSelectionChangeListener(this);
        racesByIdentifier = new HashMap<RaceIdentifier, RaceDTO>();
        selectedRaceIdentifier = raceSelectionProvider.getSelectedRaces().iterator().next();
        this.setRaceBoardName(selectedRaceIdentifier.getRaceName());
        this.errorReporter = errorReporter;
        this.userAgentType = userAgentType;
        FlowPanel mainPanel = new FlowPanel();
        mainPanel.setSize("100%", "100%");
        setWidget(mainPanel);

        timer = new Timer(PlayModes.Replay, /* delayBetweenAutoAdvancesInMilliseconds */1000);
        componentViewers = new ArrayList<ComponentViewer>();
        competitorSelectionModel = new CompetitorSelectionModel(/* hasMultiSelection */ true);

        switch (viewMode) {
            case CASCADING:
                createCascadingView(leaderboardName, leaderboardGroupName, mainPanel);
                break;
            case ONE_SCREEN:
                createOneScreenView(leaderboardName, leaderboardGroupName, mainPanel);
                break;
        }

        for (ComponentViewer componentViewer : componentViewers) {
            mainPanel.add(componentViewer.getViewerWidget());
            addComponentViewerMenuEntry(componentViewer);
        }


        RaceTimesInfoProvider raceTimesInfoProvider = new RaceTimesInfoProvider(sailingService, errorReporter, null, timer.getRefreshInterval());
        timePanel = new RaceTimePanel(timer, stringMessages, raceTimesInfoProvider);
        raceTimesInfoProvider.addRaceTimesInfoChangeListener(timePanel);
        raceSelectionProvider.addRaceSelectionChangeListener(timePanel);
        timePanel.onRaceSelectionChange(raceSelectionProvider.getSelectedRaces());
    }
    
    private void createOneScreenView(String leaderboardName, String leaderboardGroupName, FlowPanel mainPanel) {
        componentsNavigationPanel = new FlowPanel();
        componentsNavigationPanel.addStyleName("raceBoardNavigation");

        // create the default leaderboard and select the right race

        LeaderboardSettings leaderBoardSettings = LeaderboardSettingsFactory.getInstance().createNewSettingsForPlayMode(timer.getPlayMode());
        LeaderboardPanel leaderboardPanel = new LeaderboardPanel(sailingService, leaderBoardSettings, selectedRaceIdentifier, competitorSelectionModel,
                timer, leaderboardName, leaderboardGroupName, errorReporter, stringMessages, userAgentType);
        RaceMap raceMap = new RaceMap(sailingService, errorReporter, timer, competitorSelectionModel, stringMessages);

        SideBySideComponentViewer leaderboardAndMapViewer = new SideBySideComponentViewer(leaderboardPanel, raceMap, "auto", "500px");  
        componentViewers.add(leaderboardAndMapViewer);

        raceMap.onRaceSelectionChange(Collections.singletonList(selectedRaceIdentifier));
            
        WindChartSettings windChartSettings = new WindChartSettings(WindSource.values());
        WindChart windChart = new WindChart(sailingService, raceSelectionProvider, timer, windChartSettings,
                stringMessages, errorReporter);
        SimpleComponentViewer<WindChartSettings> windChartViewer = new SimpleComponentViewer<WindChartSettings>(
                windChart, "auto", "200px");
        windChart.onRaceSelectionChange(raceSelectionProvider.getSelectedRaces());
        componentViewers.add(windChartViewer);

        MultiChartPanel competitorCharts = new MultiChartPanel(sailingService, competitorSelectionModel, raceSelectionProvider,
                    timer, stringMessages, errorReporter);
            SimpleComponentViewer<MultiChartSettings> chartViewer = new SimpleComponentViewer<MultiChartSettings>(
                    competitorCharts, "auto", "200px");

            competitorCharts.onRaceSelectionChange(raceSelectionProvider.getSelectedRaces());
            componentViewers.add(chartViewer);
    }

    private void createCascadingView(String leaderboardName, String leaderboardGroupName, FlowPanel mainPanel) {
        // create the breadcrumb navigation
        breadcrumbPanel = createBreadcrumbPanel(leaderboardGroupName);
        mainPanel.add(breadcrumbPanel);

        componentsNavigationPanel = new FlowPanel();
        componentsNavigationPanel.addStyleName("raceBoardNavigation");

        boolean showLeaderboard = true;
        boolean showMap = true;
        boolean showCompetitorCharts = true;
        // create the default leaderboard and select the right race
        if(showLeaderboard) {
            LeaderboardSettings leaderBoardSettings = LeaderboardSettingsFactory.getInstance().createNewSettingsForPlayMode(timer.getPlayMode());
            LeaderboardPanel leaderboardPanel = new LeaderboardPanel(sailingService, leaderBoardSettings, selectedRaceIdentifier, competitorSelectionModel,
                    timer, leaderboardName, leaderboardGroupName, errorReporter, stringMessages, userAgentType);

            CollapsableComponentViewer<LeaderboardSettings> leaderboardViewer = new CollapsableComponentViewer<LeaderboardSettings>(
                    leaderboardPanel, "100%", "100%", stringMessages, ViewerPanelTypes.SCROLL_PANEL);
            componentViewers.add(leaderboardViewer);
        }

        // create the race map
        if(showMap) {
            RaceMap raceMap = new RaceMap(sailingService, errorReporter, timer, competitorSelectionModel, stringMessages);
            CollapsableComponentViewer<RaceMapSettings> raceMapViewer = new CollapsableComponentViewer<RaceMapSettings>(
                    raceMap, "auto", "500px", stringMessages);

            //((Panel) raceMapViewer.getViewerWidget().getContent()).add(raceMap);
            raceMap.onRaceSelectionChange(Collections.singletonList(selectedRaceIdentifier));
            componentViewers.add(raceMapViewer);
        }

        WindChartSettings windChartSettings = new WindChartSettings(WindSource.values());
        WindChart windChart = new WindChart(sailingService, raceSelectionProvider, timer, windChartSettings,
                stringMessages, errorReporter);
        CollapsableComponentViewer<WindChartSettings> windChartViewer = new CollapsableComponentViewer<WindChartSettings>(
                windChart, "auto", "400px", stringMessages);
        windChart.onRaceSelectionChange(raceSelectionProvider.getSelectedRaces());
        componentViewers.add(windChartViewer);
        if (showCompetitorCharts) {
            // DON'T DELETE -> this is temporary for testing of different chart types
//            ChartPanel competitorCharts = new ChartPanel(sailingService, competitorSelectionModel, raceSelectionProvider,
//                    timer, DetailType.WINDWARD_DISTANCE_TO_OVERALL_LEADER, stringMessages, errorReporter);
//            CollapsableComponentViewer<ChartSettings> chartViewer = new CollapsableComponentViewer<ChartSettings>(
//                    competitorCharts, "auto", "400px", stringMessages);

            MultiChartPanel competitorCharts = new MultiChartPanel(sailingService, competitorSelectionModel, raceSelectionProvider,
                    timer, stringMessages, errorReporter);
            CollapsableComponentViewer<MultiChartSettings> chartViewer = new CollapsableComponentViewer<MultiChartSettings>(
                    competitorCharts, "auto", "400px", stringMessages);

            competitorCharts.onRaceSelectionChange(raceSelectionProvider.getSelectedRaces());
            componentViewers.add(chartViewer);
        }
        
    }

    private BreadcrumbPanel createBreadcrumbPanel(String leaderboardGroupName) {
        ArrayList<Pair<String, String>> breadcrumbLinksData = new ArrayList<Pair<String, String>>();
        String debugParam = Window.Location.getParameter("gwt.codesvr");

        if(leaderboardGroupName != null) {
            String link = "/gwt/Spectator.html?leaderboardGroupName=" + leaderboardGroupName +
                    (debugParam != null && !debugParam.isEmpty() ? "&gwt.codesvr=" + debugParam : "");
            breadcrumbLinksData.add(new Pair<String, String>(link, leaderboardGroupName));
        }
        return new BreadcrumbPanel(breadcrumbLinksData, selectedRaceIdentifier.getRaceName());
    }
    
    private void addComponentViewerMenuEntry(final ComponentViewer c) {
        Anchor menuEntry = new Anchor(c.getViewerName());
        menuEntry.addStyleName("raceBoardNavigation-navigationitem");
        
        menuEntry.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.scrollTo(Window.getScrollLeft(), c.getViewerWidget().getAbsoluteTop() - scrollOffset);
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
    
    public int getScrollOffset() {
        return scrollOffset;
    }
    
    /**
     * Sets the offset, when scrolling with the menu entry anchors (in the top right corner).<br />
     * Only the absolute value of <code>scrollOffset</code> will be used.
     * @param scrollOffset The new scrolling offset. <b>Only</b> the absolute value will be used.
     */
    public void setScrollOffset(int scrollOffset) {
        this.scrollOffset = Math.abs(scrollOffset);
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
    }

    @Override
    public void onRaceSelectionChange(List<RaceIdentifier> selectedRaces) {
    }
}

