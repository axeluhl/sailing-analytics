package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.actions.AsyncActionsExecutor;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sailing.gwt.ui.client.RaceTimePanel;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.RegattaDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TimeListener;
import com.sap.sailing.gwt.ui.client.TimeRangeWithZoomModel;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.UserAgentDetails;
import com.sap.sailing.gwt.ui.leaderboard.ExplicitRaceColumnSelectionWithPreselectedRace;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettingsFactory;
import com.sap.sailing.gwt.ui.shared.RaceDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.UserDTO;
import com.sap.sailing.gwt.ui.shared.charts.MultiChartPanel;
import com.sap.sailing.gwt.ui.shared.charts.WindChart;
import com.sap.sailing.gwt.ui.shared.charts.WindChartSettings;
import com.sap.sailing.gwt.ui.shared.components.Component;
import com.sap.sailing.gwt.ui.shared.components.ComponentViewer;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialog;
import com.sap.sailing.gwt.ui.shared.racemap.RaceMap;

/**
 * A panel showing a list of components visualizing a race from the regattas announced by calls to {@link #fillRegattas(List)}.
 * The race selection is provided by a {@link RaceSelectionProvider} for which this is a {@link RaceSelectionChangeListener listener}.
 * {@link RaceIdentifier}-based race selection changes are converted to {@link RaceDTO} objects using the {@link #racesByIdentifier}
 * map maintained during {@link #fillRegattas(List)}. The race selection provider is expected to be single selection only.
 * 
 * @author Frank Mittag, Axel Uhl (d043530)
 *
 */
public class RaceBoardPanel extends FormPanel implements RegattaDisplayer, RaceSelectionChangeListener {
    private final SailingServiceAsync sailingService;
    private final MediaServiceAsync mediaService;
    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    private String raceBoardName;
    private RaceBoardViewModes viewMode;
    
    /**
     * Updated upon each {@link #fillRegattas(List)}
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
    private final UserAgentDetails userAgent;
    private final CompetitorSelectionModel competitorSelectionModel;
    private final TimeRangeWithZoomModel timeRangeWithZoomModel; 
    private final RegattaAndRaceIdentifier selectedRaceIdentifier;

    private LeaderboardPanel leaderboardPanel;
    private WindChart windChart;
    private MultiChartPanel competitorChart;
    
    /**
     * The component viewer in <code>ONESCREEN</code> view mode. <code>null</code> if in <code>CASCADE</code> view mode
     */
    private SideBySideComponentViewer leaderboardAndMapViewer;

    private final AsyncActionsExecutor asyncActionsExecutor;
    
    private final RaceTimesInfoProvider raceTimesInfoProvider;

    public RaceBoardPanel(SailingServiceAsync sailingService, MediaServiceAsync mediaService, UserDTO theUser, Timer timer,
            RaceSelectionProvider theRaceSelectionProvider, String leaderboardName, String leaderboardGroupName,
            ErrorReporter errorReporter, final StringMessages stringMessages, UserAgentDetails userAgent,
            RaceBoardViewModes viewMode, RaceTimesInfoProvider raceTimesInfoProvider) {
        this.sailingService = sailingService;
        this.mediaService = mediaService;
        this.stringMessages = stringMessages;
        this.raceSelectionProvider = theRaceSelectionProvider;
        this.raceTimesInfoProvider = raceTimesInfoProvider;
        this.scrollOffset = 0;
        raceSelectionProvider.addRaceSelectionChangeListener(this);
        racesByIdentifier = new HashMap<RaceIdentifier, RaceDTO>();
        selectedRaceIdentifier = raceSelectionProvider.getSelectedRaces().iterator().next();
        this.setRaceBoardName(selectedRaceIdentifier.getRaceName());
        this.errorReporter = errorReporter;
        this.userAgent = userAgent;
        this.viewMode = viewMode;
        asyncActionsExecutor = new AsyncActionsExecutor();
        FlowPanel mainPanel = new FlowPanel();
        mainPanel.setSize("100%", "100%");
        setWidget(mainPanel);
        this.timer = timer;
        timeRangeWithZoomModel = new TimeRangeWithZoomModel();
        componentViewers = new ArrayList<ComponentViewer>();
        competitorSelectionModel = new CompetitorSelectionModel(/* hasMultiSelection */ true);
        componentsNavigationPanel = new FlowPanel();
        componentsNavigationPanel.addStyleName("raceBoardNavigation");
        switch (this.viewMode) {
            case ONESCREEN:
                createOneScreenView(leaderboardName, leaderboardGroupName, mainPanel);                
                getElement().getStyle().setMarginLeft(12, Unit.PX);
                getElement().getStyle().setMarginRight(12, Unit.PX);
                break;
        }
        timePanel = new RaceTimePanel(timer, timeRangeWithZoomModel, stringMessages, raceTimesInfoProvider);
        timeRangeWithZoomModel.addTimeZoomChangeListener(timePanel);
        raceTimesInfoProvider.addRaceTimesInfoProviderListener(timePanel);
        raceSelectionProvider.addRaceSelectionChangeListener(timePanel);
        timePanel.onRaceSelectionChange(raceSelectionProvider.getSelectedRaces());
    }
    
    private void createOneScreenView(String leaderboardName, String leaderboardGroupName, FlowPanel mainPanel) {
        // create the default leaderboard and select the right race
        leaderboardPanel = createLeaderboardPanel(leaderboardName, leaderboardGroupName);
        leaderboardPanel.addStyleName(LeaderboardPanel.LEADERBOARD_MARGIN_STYLE);
        RaceMap raceMap = new RaceMap(sailingService, asyncActionsExecutor, errorReporter, timer, competitorSelectionModel, stringMessages);
        raceTimesInfoProvider.addRaceTimesInfoProviderListener(raceMap);
        raceMap.onRaceSelectionChange(Collections.singletonList(selectedRaceIdentifier));
        List<Component<?>> components = new ArrayList<Component<?>>();
        competitorChart = new MultiChartPanel(sailingService, asyncActionsExecutor, competitorSelectionModel, raceSelectionProvider,
                    timer, timeRangeWithZoomModel, stringMessages, errorReporter, true, true, leaderboardGroupName, leaderboardName);
        competitorChart.setVisible(false);
        competitorChart.onRaceSelectionChange(raceSelectionProvider.getSelectedRaces());
        components.add(competitorChart);
        windChart = new WindChart(sailingService, raceSelectionProvider, timer, timeRangeWithZoomModel, new WindChartSettings(),
                stringMessages, asyncActionsExecutor, errorReporter, /* compactChart */ true);
        windChart.setVisible(false);
        windChart.onRaceSelectionChange(raceSelectionProvider.getSelectedRaces());
        components.add(windChart);
        leaderboardAndMapViewer = new SideBySideComponentViewer(leaderboardPanel, raceMap, components);  
        componentViewers.add(leaderboardAndMapViewer);
        for (ComponentViewer componentViewer : componentViewers) {
            mainPanel.add(componentViewer.getViewerWidget());
        }

        addComponentToNavigationMenu(leaderboardAndMapViewer, leaderboardPanel, true);
        addComponentToNavigationMenu(leaderboardAndMapViewer, windChart,  true);
        addComponentToNavigationMenu(leaderboardAndMapViewer, competitorChart, true);
        addComponentToNavigationMenu(leaderboardAndMapViewer, raceMap, false);

        addMediaSelectorToNavigationMenu();
        
    }

    private void addMediaSelectorToNavigationMenu() {
        MediaSelector mediaSelector = new MediaSelector(errorReporter);
        raceTimesInfoProvider.addRaceTimesInfoProviderListener(mediaSelector);
        timer.addPlayStateListener(mediaSelector);
        timer.addTimeListener(mediaSelector);
        mediaService.getMediaTracksForRace(selectedRaceIdentifier, mediaSelector);
        for (Widget widget : mediaSelector.widgets()) {
            componentsNavigationPanel.add(widget);
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
    
    private LeaderboardPanel createLeaderboardPanel(String leaderboardName, String leaderboardGroupName) {
        LeaderboardSettings leaderBoardSettings = LeaderboardSettingsFactory.getInstance()
                .createNewSettingsForPlayMode(timer.getPlayMode(),
                        /* nameOfRaceToSort */ selectedRaceIdentifier.getRaceName(),
                        /* nameOfRaceColumnToShow */ null, /* nameOfRaceToShow */ selectedRaceIdentifier.getRaceName(),
                        new ExplicitRaceColumnSelectionWithPreselectedRace(selectedRaceIdentifier),
                        /* showOverallLeaderboardsOnSamePage */ false);
        return new LeaderboardPanel(sailingService, asyncActionsExecutor, leaderBoardSettings, selectedRaceIdentifier,
                competitorSelectionModel, timer, leaderboardGroupName, leaderboardName, errorReporter, stringMessages,
                userAgent, /* showRaceDetails */ true, raceTimesInfoProvider, /* autoExpandLastRaceColumn */ false);
     }

    private <SettingsType> void addComponentToNavigationMenu(final ComponentViewer componentViewer,
            final Component<SettingsType> component, boolean withCheckbox) {
        final CheckBox checkBox= new CheckBox(component.getLocalizedShortName());
        checkBox.getElement().getStyle().setFloat(Style.Float.LEFT);

        checkBox.setEnabled(withCheckbox);
        checkBox.setValue(component.isVisible());
        checkBox.setTitle(stringMessages.showHideComponent(component.getLocalizedShortName()));
        checkBox.addStyleName("raceBoardNavigation-innerElement");

        checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> newValue) {
                // make the map invisible is this is not supported yet due to problems with disabling the center element
                // of a DockPanel
                if (component instanceof RaceMap)
                    return;

                boolean visible = checkBox.getValue();
                setComponentVisible(componentViewer, component, visible);

                if (visible && component instanceof TimeListener) {
                    // trigger the component to update its data
                    ((TimeListener) component).timeChanged(timer.getTime());
                }
            }
        });

        componentsNavigationPanel.add(checkBox);

        if(component.hasSettings()) {
            Button settingsButton = new Button("");
            settingsButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    new SettingsDialog<SettingsType>(component, stringMessages).show();
                }
            });
            settingsButton.addStyleName("raceBoardNavigation-settingsButton");
            settingsButton.getElement().getStyle().setFloat(Style.Float.LEFT);
            settingsButton.setTitle(stringMessages.settingsForComponent(component.getLocalizedShortName()));
            
            componentsNavigationPanel.add(settingsButton);
        }

    }
    
    public void addComponentAsToogleButtonToNavigationMenu(final ComponentViewer componentViewer,
            final Component<?> component) {
        final ToggleButton toggleButton = new ToggleButton(component.getLocalizedShortName(),
                "\u2713 " + component.getLocalizedShortName());
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

                if (visible && component instanceof TimeListener) {
                    // trigger the component to update its data
                    ((TimeListener) component).timeChanged(timer.getTime());
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
        case ONESCREEN:
            setComponentVisible(leaderboardAndMapViewer, competitorChart, visible);
            break;
        }
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
    public void fillRegattas(List<RegattaDTO> regattas) {
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
}

