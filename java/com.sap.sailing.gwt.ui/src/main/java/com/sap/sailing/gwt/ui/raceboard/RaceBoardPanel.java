package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.EventAndRaceIdentifier;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.gwt.ui.actions.AsyncActionsExecutor;
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
import com.sap.sailing.gwt.ui.client.UserAgentChecker.UserAgentTypes;
import com.sap.sailing.gwt.ui.leaderboard.AbstractChartPanel;
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
import com.sap.sailing.gwt.ui.shared.components.Component;
import com.sap.sailing.gwt.ui.shared.components.ComponentViewer;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialog;
import com.sap.sailing.gwt.ui.shared.racemap.RaceMap;
import com.sap.sailing.gwt.ui.shared.racemap.RaceMapSettings;

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
    private RaceBoardViewModes viewMode;
    
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
    private FlowPanel settingsPanel;
    private RaceTimePanel timePanel;
    private final Timer timer;
    private final RaceSelectionProvider raceSelectionProvider;
    private final UserAgentTypes userAgentType;
    private final CompetitorSelectionModel competitorSelectionModel;
    private final EventAndRaceIdentifier selectedRaceIdentifier;

    private AudioTool audioTool;
    private LeaderboardPanel leaderboardPanel;
    private WindChart windChart;
    private MultiChartPanel competitorChart;
    
    /**
     * The component viewer in <code>ONESCREEN</code> view mode. <code>null</code> if in <code>CASCADE</code> view mode
     */
    private SideBySideComponentViewer leaderboardAndMapViewer;

    /**
     * The leaderboard viewer in <code>CASCADE</code> view mode. <code>null</code> if in <code>ONESCREEN</code> view mode
     */
    private CollapsableComponentViewer<LeaderboardSettings> leaderboardViewer = null;

    /**
     * The wind chart viewer in <code>CASCADE</code> view mode. <code>null</code> if in <code>ONESCREEN</code> view mode
     */
    private CollapsableComponentViewer<WindChartSettings> windChartViewer = null;

    /**
     * The competitor chart viewer in <code>CASCADE</code> view mode. <code>null</code> if in <code>ONESCREEN</code> view mode
     */
    private CollapsableComponentViewer<MultiChartSettings> competitorChartViewer = null;

    private final AsyncActionsExecutor asyncActionsExecutor;
    
    private final RaceTimesInfoProvider raceTimesInfoProvider;

    public RaceBoardPanel(SailingServiceAsync sailingService, UserDTO theUser, Timer timer,
            RaceSelectionProvider theRaceSelectionProvider, String leaderboardName, String leaderboardGroupName,
            ErrorReporter errorReporter, final StringMessages stringMessages, UserAgentTypes userAgentType,
            RaceBoardViewModes viewMode, RaceTimesInfoProvider raceTimesInfoProvider) {
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.raceSelectionProvider = theRaceSelectionProvider;
        this.raceTimesInfoProvider = raceTimesInfoProvider;
        this.scrollOffset = 0;
        raceSelectionProvider.addRaceSelectionChangeListener(this);
        racesByIdentifier = new HashMap<RaceIdentifier, RaceDTO>();
        selectedRaceIdentifier = raceSelectionProvider.getSelectedRaces().iterator().next();
        this.setRaceBoardName(selectedRaceIdentifier.getRaceName());
        this.errorReporter = errorReporter;
        this.userAgentType = userAgentType;
        this.viewMode = viewMode;
        asyncActionsExecutor = new AsyncActionsExecutor();
        FlowPanel mainPanel = new FlowPanel();
        mainPanel.setSize("100%", "100%");

        setWidget(mainPanel);

        this.timer = timer;
        componentViewers = new ArrayList<ComponentViewer>();
        competitorSelectionModel = new CompetitorSelectionModel(/* hasMultiSelection */ true);

        componentsNavigationPanel = new FlowPanel();
        componentsNavigationPanel.addStyleName("raceBoardNavigation");

        switch (this.viewMode) {
            case CASCADE:
                createCascadingView(leaderboardName, leaderboardGroupName, mainPanel);
                break;
            case ONESCREEN:
                createOneScreenView(leaderboardName, leaderboardGroupName, mainPanel);                
                getElement().getStyle().setMarginLeft(12, Unit.PX);
                getElement().getStyle().setMarginRight(12, Unit.PX);
                break;
        }

        timePanel = new RaceTimePanel(timer, stringMessages, raceTimesInfoProvider);
        raceTimesInfoProvider.addRaceTimesInfoProviderListener(timePanel);
        raceSelectionProvider.addRaceSelectionChangeListener(timePanel);
        timePanel.onRaceSelectionChange(raceSelectionProvider.getSelectedRaces());
    }
    
    private void createOneScreenView(String leaderboardName, String leaderboardGroupName, FlowPanel mainPanel) {
        componentsNavigationPanel = new FlowPanel();
        componentsNavigationPanel.addStyleName("raceBoardNavigation");

        // create the default leaderboard and select the right race
        leaderboardPanel = createLeaderboardPanel(leaderboardName, leaderboardGroupName);
        RaceMap raceMap = new RaceMap(sailingService, asyncActionsExecutor, errorReporter, timer, competitorSelectionModel, stringMessages);
        raceTimesInfoProvider.addRaceTimesInfoProviderListener(raceMap);
        raceMap.onRaceSelectionChange(Collections.singletonList(selectedRaceIdentifier));

        List<Component<?>> components = new ArrayList<Component<?>>();

        competitorChart = new MultiChartPanel(sailingService, asyncActionsExecutor, competitorSelectionModel, raceSelectionProvider,
                    timer, stringMessages, errorReporter, true, true);
        competitorChart.onRaceSelectionChange(raceSelectionProvider.getSelectedRaces());
        components.add(competitorChart);
        competitorChart.setVisible(false);

        windChart = createWindChart(asyncActionsExecutor);
        raceTimesInfoProvider.addRaceTimesInfoProviderListener(windChart);
        windChart.onRaceSelectionChange(raceSelectionProvider.getSelectedRaces());
        windChart.setVisible(false);
        components.add(windChart);
        
        leaderboardAndMapViewer = new SideBySideComponentViewer(leaderboardPanel, raceMap, components);  
        componentViewers.add(leaderboardAndMapViewer);
            
        for (ComponentViewer componentViewer : componentViewers) {
            mainPanel.add(componentViewer.getViewerWidget());
        }

        //TODO binding of settings to the user agent
//        MenuBar mainMenu = new MenuBar();
//        mainMenu.setStyleName("raceBoardNavigation-navigationitem");
//        MenuBar settingsMenu = new MenuBar(true);
//        mainMenu.addItem("Settings", settingsMenu);
//        
//        addSettingsMenuItem(settingsMenu, leaderboardPanel);
//        addSettingsMenuItem(settingsMenu, raceMap);
//        addSettingsMenuItem(settingsMenu, windChart);
//        addSettingsMenuItem(settingsMenu, competitorChart);
//
//        mainMenu.getElement().getStyle().setFloat(Style.Float.LEFT);
//        mainMenu.getElement().getStyle().setPadding(3, Style.Unit.PX);
//        mainMenu.getElement().getStyle().setMargin(3, Style.Unit.PX);
//
//        componentsNavigationPanel.add(mainMenu);
        settingsPanel = new FlowPanel();
        settingsPanel.addStyleName("raceBoardNavigation-settingsButtonPanel");
        Label settingsLabel = new Label("Settings: ");
        settingsLabel.addStyleName("raceBoardNavigation-settingsLabel");
        settingsLabel.getElement().getStyle().setFloat(Style.Float.LEFT);
        settingsLabel.getElement().getStyle().setPadding(3, Style.Unit.PX);
        settingsPanel.add(settingsLabel);

        addAudioToolMenuButton(audioTool);
        addSettingsMenuButton(settingsPanel, leaderboardPanel);
        addSettingsMenuButton(settingsPanel, raceMap);
        addSettingsMenuButton(settingsPanel, windChart);
        addSettingsMenuButton(settingsPanel, competitorChart);

        addComponentAsToogleButtonToNavigationMenu(leaderboardAndMapViewer, leaderboardPanel);
        //addComponentAsToogleButtonToNavigationMenu(leaderboardAndMapViewer, raceMap);
        addComponentAsToogleButtonToNavigationMenu(leaderboardAndMapViewer, windChart);
        addComponentAsToogleButtonToNavigationMenu(leaderboardAndMapViewer, competitorChart);
        
    }

    @SuppressWarnings("unused")
    private <SettingsType> void addSettingsMenuItem(MenuBar settingsMenu, final Component<SettingsType> component) {
        if(component.hasSettings()) {
            settingsMenu.addItem(component.getLocalizedShortName(), new Command() {
                public void execute() {
                    new SettingsDialog<SettingsType>(component, stringMessages).show();
                  }
            });
        }
    }
    
    private void addAudioToolMenuButton(final AudioTool component) {
 //       if(component.hasSettings()) {
            Button settingsButton = new Button("Audio Tool");
            settingsButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                   new AudioTool();
                }
            });
            settingsButton.addStyleName("raceBoardNavigation-settingsButton");
            settingsButton.getElement().getStyle().setFloat(Style.Float.LEFT);
            settingsButton.getElement().getStyle().setPadding(3, Style.Unit.PX);
            
            settingsPanel.add(settingsButton);
//        }
    }
    private <SettingsType> void addSettingsMenuButton(FlowPanel settingsPanel, final Component<SettingsType> component) {
        if(component.hasSettings()) {
            Button settingsButton = new Button(component.getLocalizedShortName());
            settingsButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    new SettingsDialog<SettingsType>(component, stringMessages).show();
                }
            });
            settingsButton.addStyleName("raceBoardNavigation-settingsButton");
            settingsButton.getElement().getStyle().setFloat(Style.Float.LEFT);
            settingsButton.getElement().getStyle().setPadding(3, Style.Unit.PX);
            
            settingsPanel.add(settingsButton);
        }
    }
    
    private void createCascadingView(String leaderboardName, String leaderboardGroupName, FlowPanel mainPanel) {
        boolean showLeaderboard = true;
        boolean showMap = true;
        boolean showCompetitorCharts = true;
        // create the default leaderboard and select the right race
        if(showLeaderboard) {
            leaderboardPanel = createLeaderboardPanel(leaderboardName, leaderboardGroupName);
            leaderboardViewer = new CollapsableComponentViewer<LeaderboardSettings>(
                    leaderboardPanel, "100%", "100%", stringMessages, ViewerPanelTypes.SCROLL_PANEL);
            componentViewers.add(leaderboardViewer);
        }

        // create the race map
        if(showMap) {
            RaceMap raceMap = new RaceMap(sailingService, asyncActionsExecutor, errorReporter, timer, competitorSelectionModel, stringMessages);
            CollapsableComponentViewer<RaceMapSettings> raceMapViewer = new CollapsableComponentViewer<RaceMapSettings>(
                    raceMap, "auto", "500px", stringMessages);

            raceTimesInfoProvider.addRaceTimesInfoProviderListener(raceMap);
            raceMap.onRaceSelectionChange(Collections.singletonList(selectedRaceIdentifier));
            componentViewers.add(raceMapViewer);
        }

        windChart = createWindChart(asyncActionsExecutor);
        windChartViewer = new CollapsableComponentViewer<WindChartSettings>(
                windChart, "auto", "400px", stringMessages);
        raceTimesInfoProvider.addRaceTimesInfoProviderListener(windChart);
        windChart.onRaceSelectionChange(raceSelectionProvider.getSelectedRaces());
        componentViewers.add(windChartViewer);
        if (showCompetitorCharts) {
            // DON'T DELETE -> this is temporary for testing of different chart types
//            ChartPanel competitorCharts = new ChartPanel(sailingService, competitorSelectionModel, raceSelectionProvider,
//                    timer, DetailType.WINDWARD_DISTANCE_TO_OVERALL_LEADER, stringMessages, errorReporter);
//            CollapsableComponentViewer<ChartSettings> chartViewer = new CollapsableComponentViewer<ChartSettings>(
//                    competitorCharts, "auto", "400px", stringMessages);

            competitorChart = new MultiChartPanel(sailingService, asyncActionsExecutor, competitorSelectionModel, raceSelectionProvider,
                    timer, stringMessages, errorReporter, false, true);
            competitorChartViewer = new CollapsableComponentViewer<MultiChartSettings>(
                    competitorChart, "auto", "400px", stringMessages);

            competitorChart.onRaceSelectionChange(raceSelectionProvider.getSelectedRaces());
            componentViewers.add(competitorChartViewer);
        }

        for (ComponentViewer componentViewer : componentViewers) {
            mainPanel.add(componentViewer.getViewerWidget());
            addComponentViewerAsAnchorToNavigationMenu(componentViewer);
        }
    }

    private LeaderboardPanel createLeaderboardPanel(String leaderboardName, String leaderboardGroupName) {
        LeaderboardSettings leaderBoardSettings = LeaderboardSettingsFactory.getInstance()
                .createNewSettingsForPlayMode(timer.getPlayMode(), /* nameOfRaceToSort */
                        selectedRaceIdentifier.getRaceName(),
                        /* nameOfRaceColumnToShow */null, /* nameOfRaceToShow */selectedRaceIdentifier.getRaceName());
        return new LeaderboardPanel(sailingService, asyncActionsExecutor, leaderBoardSettings, selectedRaceIdentifier,
                competitorSelectionModel, timer, leaderboardName, leaderboardGroupName, errorReporter, stringMessages,
                userAgentType);
     }

    private WindChart createWindChart(AsyncActionsExecutor asyncActionsExecutor) {
        WindChartSettings windChartSettings = new WindChartSettings(false, true, new HashSet<WindSourceType>(Arrays.asList(WindSourceType.values())));
        return new WindChart(sailingService, raceSelectionProvider, timer, windChartSettings,
                stringMessages, asyncActionsExecutor, errorReporter, viewMode == RaceBoardViewModes.ONESCREEN);
    }

    private void addComponentAsToogleButtonToNavigationMenu(final ComponentViewer componentViewer,
            final Component<?> component) {
        final ToggleButton toggleButton = new ToggleButton(component.getLocalizedShortName(),
                component.getLocalizedShortName());
        toggleButton.getElement().getStyle().setFloat(Style.Float.LEFT);
        toggleButton.setDown(component.isVisible());
        toggleButton.setTitle(stringMessages.showHideComponent(component.getLocalizedShortName()));

        toggleButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                // make the map invisible is this is not supported yet due to problems with disabling the center element
                // of a DockPanel
                if (component instanceof RaceMap)
                    return;

                boolean visible = toggleButton.isDown();
                setComponentVisible(componentViewer, component, visible);

                // Forcing a chart time line update and a load of the data, or it wouldn't be displayed if the chart is
                // set to visible
                if (visible && component instanceof WindChart) {
                    windChart.timeChanged(timer.getTime());
                } else if (visible && component instanceof AbstractChartPanel) {
                    competitorChart.triggerDataLoading();
                } else if (visible && component instanceof LeaderboardPanel) {
                    leaderboardPanel.timeChanged(timer.getTime());
                }
            }
        });

        componentsNavigationPanel.add(toggleButton);
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
        switch (viewMode) {
        case CASCADE:
            leaderboardViewer.getViewerWidget().setOpen(visible);
            break;
        case ONESCREEN:
            setComponentVisible(leaderboardAndMapViewer, leaderboardPanel, visible);
            break;
        }
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
        switch (viewMode) {
        case CASCADE:
            windChartViewer.getViewerWidget().setOpen(visible);
            break;
        case ONESCREEN:
            setComponentVisible(leaderboardAndMapViewer, windChart, visible);
            break;
        }
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
        switch (viewMode) {
        case CASCADE:
            competitorChartViewer.getViewerWidget().setOpen(visible);
            break;
        case ONESCREEN:
            setComponentVisible(leaderboardAndMapViewer, competitorChart, visible);
            break;
        }
    }
    
    private void addComponentViewerAsAnchorToNavigationMenu(final ComponentViewer componentViewer) {
        Anchor menuEntry = new Anchor(componentViewer.getViewerName());
        menuEntry.addStyleName("raceBoardNavigation-navigationitem");
        
        menuEntry.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.scrollTo(Window.getScrollLeft(), componentViewer.getViewerWidget().getAbsoluteTop() - scrollOffset);
            }
        });
        componentsNavigationPanel.add(menuEntry);
    }

    public Widget getNavigationWidget() {
        return componentsNavigationPanel; 
    }
    
    public Widget getSettingsWidget() {
        return settingsPanel;
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
    public void onRaceSelectionChange(List<EventAndRaceIdentifier> selectedRaces) {
    }
}

