package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.UIObject;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.EntryPointLinkFactory;
import com.sap.sailing.gwt.ui.client.GlobalNavigationPanel;
import com.sap.sailing.gwt.ui.client.LeaderboardUpdateListener;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sailing.gwt.ui.client.RaceTimePanel;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.media.MediaPlayerManagerComponent;
import com.sap.sailing.gwt.ui.client.media.PopupPositionProvider;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorRaceChart;
import com.sap.sailing.gwt.ui.client.shared.charts.WindChart;
import com.sap.sailing.gwt.ui.client.shared.charts.WindChartSettings;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.client.shared.components.ComponentViewer;
import com.sap.sailing.gwt.ui.client.shared.components.Perspective;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialog;
import com.sap.sailing.gwt.ui.client.shared.filter.FilterWithUI;
import com.sap.sailing.gwt.ui.client.shared.filter.LeaderboardFetcher;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;
import com.sap.sailing.gwt.ui.leaderboard.CompetitorFilterPanel;
import com.sap.sailing.gwt.ui.leaderboard.ExplicitRaceColumnSelectionWithPreselectedRace;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettingsFactory;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.common.filter.FilterSet;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.URLEncoder;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.TimeRangeWithZoomModel;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;
import com.sap.sse.security.ui.client.UserService;

/**
 * A view showing a list of components visualizing a race from the regattas announced by calls to {@link #fillRegattas(List)}.
 * The race selection is provided by a {@link RaceSelectionProvider} for which this is a {@link RaceSelectionChangeListener listener}.
 * {@link RaceIdentifier}-based race selection changes are converted to {@link RaceDTO} objects using the {@link #racesByIdentifier}
 * map maintained during {@link #fillRegattas(List)}. The race selection provider is expected to be single selection only.
 * 
 * @author Frank Mittag, Axel Uhl (d043530)
 *
 */
public class RaceBoardPanel extends SimplePanel implements RegattasDisplayer, RaceSelectionChangeListener, 
    LeaderboardUpdateListener, PopupPositionProvider, Perspective {
    private final SailingServiceAsync sailingService;
    private final MediaServiceAsync mediaService;
    private final EventDTO event;
    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    private final RaceBoardViewConfiguration raceboardViewConfiguration;
    private String raceBoardName;
    
    /**
     * Updated upon each {@link #fillRegattas(List)}
     */
    private final Map<RaceIdentifier, RaceDTO> racesByIdentifier;
    
    private RaceTimePanel timePanel;
    private final Timer timer;
    private final RaceSelectionProvider raceSelectionProvider;
    private final UserAgentDetails userAgent;
    private final CompetitorSelectionModel competitorSelectionModel;
    private final TimeRangeWithZoomModel timeRangeWithZoomModel; 
    private final RegattaAndRaceIdentifier selectedRaceIdentifier;

    private final String leaderboardName;
    private final LeaderboardPanel leaderboardPanel;
    private WindChart windChart;
    private MultiCompetitorRaceChart competitorChart;
    
    /**
     * The component viewer in <code>ONESCREEN</code> view mode. <code>null</code> if in <code>CASCADE</code> view mode
     */
    private SideBySideComponentViewer leaderboardAndMapViewer;

    private final AsyncActionsExecutor asyncActionsExecutor;
    
    private final RaceTimesInfoProvider raceTimesInfoProvider;
    private RaceMap raceMap;
    
    private final FlowPanel raceInformationHeader;
    private final FlowPanel regattaAndRaceTimeInformationHeader;
    private boolean currentRaceHasBeenSelectedOnce;
    
    private final List<Component<?>> components;
    
    /**
     * @param event
     *            an optional event that can be used for "back"-navigation in case the race board shows a race in the
     *            context of an event; may be <code>null</code>.
     */
    public RaceBoardPanel(SailingServiceAsync sailingService, MediaServiceAsync mediaService,
            UserService userService, AsyncActionsExecutor asyncActionsExecutor,
            Timer timer, RaceSelectionProvider theRaceSelectionProvider, String leaderboardName,
            String leaderboardGroupName, EventDTO event, RaceBoardViewConfiguration raceboardViewConfiguration,
            ErrorReporter errorReporter, final StringMessages stringMessages,
            UserAgentDetails userAgent, RaceTimesInfoProvider raceTimesInfoProvider, boolean showMapControls) {
        this.sailingService = sailingService;
        this.mediaService = mediaService;
        this.stringMessages = stringMessages;
        this.raceboardViewConfiguration = raceboardViewConfiguration;
        this.raceSelectionProvider = theRaceSelectionProvider;
        this.raceTimesInfoProvider = raceTimesInfoProvider;
        this.errorReporter = errorReporter;
        this.userAgent = userAgent;
        this.timer = timer;
        this.event = event;
        this.currentRaceHasBeenSelectedOnce = false;
        this.leaderboardName = leaderboardName;
        this.components = new ArrayList<Component<?>>();
        raceSelectionProvider.addRaceSelectionChangeListener(this);
        racesByIdentifier = new HashMap<RaceIdentifier, RaceDTO>();
        selectedRaceIdentifier = raceSelectionProvider.getSelectedRaces().iterator().next();
        this.setRaceBoardName(selectedRaceIdentifier.getRaceName());
        this.asyncActionsExecutor = asyncActionsExecutor;
        FlowPanel mainPanel = new FlowPanel();
        mainPanel.setSize("100%", "100%");
        setWidget(mainPanel);
        raceInformationHeader = new FlowPanel();
        raceInformationHeader.setStyleName("RegattaRaceInformation-Header");
        regattaAndRaceTimeInformationHeader = new FlowPanel();
        regattaAndRaceTimeInformationHeader.setStyleName("RegattaAndRaceTime-Header");
        timeRangeWithZoomModel = new TimeRangeWithZoomModel();
        competitorSelectionModel = new CompetitorSelectionModel(/* hasMultiSelection */ true);
                
        raceMap = new RaceMap(sailingService, asyncActionsExecutor, errorReporter, timer,
                competitorSelectionModel, stringMessages, showMapControls, getConfiguration().isShowViewStreamlets(), getConfiguration().isShowViewSimulation(), selectedRaceIdentifier);
        components.add(raceMap);
        CompetitorFilterPanel competitorSearchTextBox = new CompetitorFilterPanel(competitorSelectionModel, stringMessages, raceMap,
                new LeaderboardFetcher() {
                    @Override
                    public LeaderboardDTO getLeaderboard() {
                        return leaderboardPanel.getLeaderboard();
                    }
                }, selectedRaceIdentifier);
        raceMap.getLeftHeaderPanel().add(raceInformationHeader);
        raceMap.getRightHeaderPanel().add(regattaAndRaceTimeInformationHeader);

        leaderboardPanel = createLeaderboardPanel(leaderboardName, leaderboardGroupName, competitorSearchTextBox);
        components.add(leaderboardPanel);
        leaderboardPanel.setTitle(stringMessages.leaderboard());
        leaderboardPanel.getElement().getStyle().setMarginLeft(6, Unit.PX);
        leaderboardPanel.getElement().getStyle().setMarginTop(4, Unit.PX);
        createOneScreenView(leaderboardName, leaderboardGroupName, event, mainPanel, showMapControls, raceMap, userService); // initializes the raceMap field
        leaderboardPanel.addLeaderboardUpdateListener(this);
        // in case the URL configuration contains the name of a competitors filter set we try to activate it
        // FIXME the competitorsFilterSets has now moved to CompetitorSearchTextBox (which should probably be renamed); pass on the parameters to the LeaderboardPanel and see what it does with it
        if (raceboardViewConfiguration.getActiveCompetitorsFilterSetName() != null) {
            for (FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> filterSet : competitorSearchTextBox.getCompetitorsFilterSets()
                    .getFilterSets()) {
                if (filterSet.getName().equals(raceboardViewConfiguration.getActiveCompetitorsFilterSetName())) {
                    competitorSearchTextBox.getCompetitorsFilterSets().setActiveFilterSet(filterSet);
                    break;
                }
            }
        }
        timePanel = new RaceTimePanel(timer, timeRangeWithZoomModel, stringMessages, raceTimesInfoProvider,
                raceboardViewConfiguration.isCanReplayDuringLiveRaces());
        timeRangeWithZoomModel.addTimeZoomChangeListener(timePanel);
        raceTimesInfoProvider.addRaceTimesInfoProviderListener(timePanel);
        raceSelectionProvider.addRaceSelectionChangeListener(timePanel);
        timePanel.onRaceSelectionChange(raceSelectionProvider.getSelectedRaces());
    }
    
    /**
     * @param event an optional event; may be <code>null</code> or else can be used to show some context information in
     * the {@link GlobalNavigationPanel}.
     */
    private void createOneScreenView(String leaderboardName, String leaderboardGroupName, EventDTO event, FlowPanel mainPanel,
            boolean showMapControls, RaceMap raceMap, UserService userService) {
        // create the default leaderboard and select the right race
        raceTimesInfoProvider.addRaceTimesInfoProviderListener(raceMap);
        raceMap.onRaceSelectionChange(Collections.singletonList(selectedRaceIdentifier));
        List<Component<?>> componentsForSideBySideViewer = new ArrayList<Component<?>>();
        competitorChart = new MultiCompetitorRaceChart(sailingService, asyncActionsExecutor, competitorSelectionModel, raceSelectionProvider,
                    timer, timeRangeWithZoomModel, stringMessages, errorReporter, true, true, leaderboardGroupName, leaderboardName);
        competitorChart.onRaceSelectionChange(raceSelectionProvider.getSelectedRaces());
        competitorChart.getEntryWidget().setTitle(stringMessages.competitorCharts());
        competitorChart.setVisible(false);
        componentsForSideBySideViewer.add(competitorChart);
        windChart = new WindChart(sailingService, raceSelectionProvider, timer, timeRangeWithZoomModel, new WindChartSettings(),
                stringMessages, asyncActionsExecutor, errorReporter, /* compactChart */ true);
        
        windChart.setVisible(false);
        windChart.onRaceSelectionChange(raceSelectionProvider.getSelectedRaces());
        windChart.getEntryWidget().setTitle(stringMessages.windChart());
        componentsForSideBySideViewer.add(windChart);
        boolean autoSelectMedia = getConfiguration().isAutoSelectMedia();
        MediaPlayerManagerComponent mediaPlayerManagerComponent = new MediaPlayerManagerComponent(
                selectedRaceIdentifier, raceTimesInfoProvider, timer, mediaService, userService, stringMessages,
                errorReporter, userAgent, this, autoSelectMedia);
        leaderboardAndMapViewer = new SideBySideComponentViewer(leaderboardPanel, raceMap, mediaPlayerManagerComponent, componentsForSideBySideViewer, stringMessages);
        components.addAll(componentsForSideBySideViewer);
        mainPanel.add(leaderboardAndMapViewer.getViewerWidget());
        boolean showLeaderboard = getConfiguration().isShowLeaderboard();
        if (Document.get().getClientWidth() <= 1024) {
            showLeaderboard = false;
        }
        setLeaderboardVisible(showLeaderboard);
        setWindChartVisible(getConfiguration().isShowWindChart());
        setCompetitorChartVisible(getConfiguration().isShowCompetitorsChart());
        // make sure to load leaderboard data for filtering to work
        if (!showLeaderboard) {
            leaderboardPanel.setVisible(true);
            leaderboardPanel.setVisible(false);
        }
    }
    
    @SuppressWarnings("unused")
    private <SettingsType> void addSettingsMenuItem(MenuBar settingsMenu, final Component<SettingsType> component) {
        if (component.hasSettings()) {
            settingsMenu.addItem(component.getLocalizedShortName(), new Command() {
                public void execute() {
                    new SettingsDialog<SettingsType>(component, stringMessages).show();
                  }
            });
        }
    }
    
    private LeaderboardPanel createLeaderboardPanel(String leaderboardName, String leaderboardGroupName, CompetitorFilterPanel competitorSearchTextBox) {
        LeaderboardSettings leaderBoardSettings = LeaderboardSettingsFactory.getInstance()
                .createNewSettingsForPlayMode(timer.getPlayMode(),
                        /* nameOfRaceToSort */ selectedRaceIdentifier.getRaceName(),
                        /* nameOfRaceColumnToShow */ null, /* nameOfRaceToShow */ selectedRaceIdentifier.getRaceName(),
                        new ExplicitRaceColumnSelectionWithPreselectedRace(selectedRaceIdentifier), /* showRegattaRank */ false);
        return new LeaderboardPanel(sailingService, asyncActionsExecutor, leaderBoardSettings, selectedRaceIdentifier != null, selectedRaceIdentifier,
                competitorSelectionModel, timer, leaderboardGroupName, leaderboardName, errorReporter, stringMessages,
                userAgent, /* showRaceDetails */ true, competitorSearchTextBox,
                /* showSelectionCheckbox */ true, raceTimesInfoProvider, /* autoExpandLastRaceColumn */ false,
                /* don't adjust the timer's delay from the leaderboard; control it solely from the RaceTimesInfoProvider */ false,
                /*autoApplyTopNFilter*/ false, /*showCompetitorFilterStatus*/ false);
    }

    private void setComponentVisible(ComponentViewer componentViewer, Component<?> component, boolean visible) {
        component.setVisible(visible);      
        componentViewer.forceLayout();
    }
    
    /**
     * Sets the collapsable panel for the leaderboard open or close, if in <code>CASCADE</code> view mode.<br />
     * Displays or hides the leaderboard, if in <code>ONESCREEN</code> view mode.<br /><br />
     * 
     * The race board should be completely rendered before this method is called, or a few exceptions could be thrown.
     * 
     * @param visible <code>true</code> if the leaderboard shall be open/visible
     */
    public void setLeaderboardVisible(boolean visible) {
        setComponentVisible(leaderboardAndMapViewer, leaderboardPanel, visible);
    }

    /**
     * Sets the collapsable panel for the wind chart open or close, if in <code>CASCADE</code> view mode.<br />
     * Displays or hides the wind chart, if in <code>ONESCREEN</code> view mode.<br /><br />
     * 
     * The race board should be completely rendered before this method is called, or a few exceptions could be thrown.
     * 
     * @param visible <code>true</code> if the wind chart shall be open/visible
     */
    public void setWindChartVisible(boolean visible) {
        setComponentVisible(leaderboardAndMapViewer, windChart, visible);
    }

    /**
     * Sets the collapsable panel for the competitor chart open or close, if in <code>CASCADE</code> view mode.<br />
     * Displays or hides the competitor chart, if in <code>ONESCREEN</code> view mode.<br /><br />
     * 
     * The race board should be completely rendered before this method is called, or a few exceptions could be thrown.
     * 
     * @param visible <code>true</code> if the competitor chart shall be open/visible
     */
    public void setCompetitorChartVisible(boolean visible) {
        setComponentVisible(leaderboardAndMapViewer, competitorChart, visible);
    }
    
    public RaceTimePanel getTimePanel() {
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
    public void fillRegattas(Iterable<RegattaDTO> regattas) {
        racesByIdentifier.clear();
        for (RegattaDTO regatta : regattas) {
            for (RaceDTO race : regatta.races) {
                if (race != null && race.getRaceIdentifier() != null) {
                    racesByIdentifier.put(race.getRaceIdentifier(), race);
                }
            }
        }
    }

    @Override
    public void onRaceSelectionChange(List<RegattaAndRaceIdentifier> selectedRaces) {
    }

    public RaceBoardViewConfiguration getConfiguration() {
        return raceboardViewConfiguration;
    }

    @Override
    public void updatedLeaderboard(LeaderboardDTO leaderboard) {
        leaderboardAndMapViewer.setLeftComponentWidth(leaderboardPanel.getContentPanel().getOffsetWidth());
    }

    @Override
    public void currentRaceSelected(RaceIdentifier raceIdentifier, RaceColumnDTO raceColumn) {
        if (!currentRaceHasBeenSelectedOnce) {
            FleetDTO fleet = raceColumn.getFleet(raceIdentifier);
            String seriesName = raceColumn.getSeriesName();
            if (LeaderboardNameConstants.DEFAULT_SERIES_NAME.equals(seriesName)) {
                seriesName = "";
            } 
            String fleetForRaceName = fleet==null?"":fleet.getName();
            if (fleetForRaceName.equals(LeaderboardNameConstants.DEFAULT_FLEET_NAME)) {
                fleetForRaceName = "";
            } else {
                fleetForRaceName = " - "+fleetForRaceName;
            }
            final Label raceNameLabel = new Label(stringMessages.race() + " " + raceColumn.getRaceColumnName());
            raceNameLabel.setStyleName("RaceName-Label");
            final Label raceAdditionalInformationLabel = new Label(seriesName + fleetForRaceName);
            raceAdditionalInformationLabel.setStyleName("RaceSeriesAndFleet-Label");
            raceInformationHeader.clear();
            raceInformationHeader.add(raceNameLabel);
            raceInformationHeader.add(raceAdditionalInformationLabel);
            final Anchor regattaNameAnchor = new Anchor(raceIdentifier.getRegattaName());
            if (event != null) {
                // we don't use the EntryPointLinkFactory here, because of lacking support for Places
                String debugParam = Window.Location.getParameter("gwt.codesvr");
                String link = "/gwt/Home.html";
                if (debugParam != null && !debugParam.isEmpty()) {
                    link += "?gwt.codesvr=" + debugParam;
                }
                link += "#EventPlace:eventId="+event.id.toString();
                link += "&navigationTab=Regatta&leaderboardName=" + URLEncoder.encode(leaderboardName);
                regattaNameAnchor.setHref(link);
            } else {
                String leaderboardGroupNameParam = Window.Location.getParameter("leaderboardGroupName");
                if(leaderboardGroupNameParam != null) {
                    Map<String, String> leaderboardGroupLinkParameters = new HashMap<String, String>();
                    leaderboardGroupLinkParameters.put("showRaceDetails", "true");
                    leaderboardGroupLinkParameters.put("leaderboardGroupName", leaderboardGroupNameParam);
                    String leaderBoardGroupLink = EntryPointLinkFactory.createLeaderboardGroupLink(leaderboardGroupLinkParameters);
                    regattaNameAnchor.setHref(leaderBoardGroupLink); 
                } else {
                    // fallback 
                    regattaNameAnchor.setHref("javascript:window.history.back();"); 
                }
            }
            regattaNameAnchor.setStyleName("RegattaName-Anchor");

            // TODO: Strange behavior... check
//            Window.addResizeHandler(new ResizeHandler() {
//                @Override
//                public void onResize(ResizeEvent event) {
//                    int headerPanelWidth = raceMap.getRightHeaderPanel().getOffsetWidth() - 150; // 150px is the width of the sapLogo 
//                    int raceNameAndFleetLabelWidth = raceInformationHeader.getOffsetWidth();
//                    int regattaAnchorWidth = regattaNameAnchor.getOffsetWidth();
//                    boolean overlap = raceNameAndFleetLabelWidth + regattaAnchorWidth > headerPanelWidth;  
//                    raceInformationHeader.setVisible(!overlap);
//                }
//            });

            Label raceTimeLabel = computeRaceInformation(raceColumn, fleet);
            raceTimeLabel.setStyleName("RaceTime-Label");
            regattaAndRaceTimeInformationHeader.clear();
            regattaAndRaceTimeInformationHeader.add(regattaNameAnchor);
            regattaAndRaceTimeInformationHeader.add(raceTimeLabel);
            currentRaceHasBeenSelectedOnce = true;
        }
    }

    @Override
    public UIObject getXPositionUiObject() {
        return timePanel;
    }

    @Override
    public UIObject getYPositionUiObject() {
        return timePanel;
    }

    private Label computeRaceInformation(RaceColumnDTO raceColumn, FleetDTO fleet) {
        Label raceInformationLabel = new Label();
        raceInformationLabel.setStyleName("Race-Time-Label");
        DateTimeFormat formatter = DateTimeFormat.getFormat("E d/M/y");
        raceInformationLabel.setText(formatter.format(raceColumn.getStartDate(fleet)));
        return raceInformationLabel;
    }

    @Override
    public Iterable<Component<?>> getComponents() {
        return components;
    }
    
    @Override
    public String getPerspectiveName() {
        return "Raceboard";
    }
}

