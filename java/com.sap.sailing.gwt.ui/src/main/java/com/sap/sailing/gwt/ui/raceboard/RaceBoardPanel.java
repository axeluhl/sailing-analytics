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
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.filter.BinaryOperator;
import com.sap.sailing.domain.common.filter.Filter;
import com.sap.sailing.domain.common.filter.FilterSet;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.RaceDTO;
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
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorRaceChart;
import com.sap.sailing.gwt.ui.client.shared.charts.WindChart;
import com.sap.sailing.gwt.ui.client.shared.charts.WindChartSettings;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.client.shared.components.ComponentViewer;
import com.sap.sailing.gwt.ui.client.media.MediaSelector;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialog;
import com.sap.sailing.gwt.ui.client.shared.filter.CompetitorRaceRankFilter;
import com.sap.sailing.gwt.ui.client.shared.filter.CompetitorSelectionProviderFilterContext;
import com.sap.sailing.gwt.ui.client.shared.filter.CompetitorTotalRankFilter;
import com.sap.sailing.gwt.ui.client.shared.filter.CompetitorsFilterSets;
import com.sap.sailing.gwt.ui.client.shared.filter.CompetitorsFilterSetsDialog;
import com.sap.sailing.gwt.ui.client.shared.filter.CompetitorsFilterSetsJsonDeSerializer;
import com.sap.sailing.gwt.ui.client.shared.filter.FilterWithUI;
import com.sap.sailing.gwt.ui.client.shared.filter.LeaderboardFilterContext;
import com.sap.sailing.gwt.ui.client.shared.filter.SelectedCompetitorsFilter;
import com.sap.sailing.gwt.ui.client.shared.filter.SelectedRaceFilterContext;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;
import com.sap.sailing.gwt.ui.leaderboard.ExplicitRaceColumnSelectionWithPreselectedRace;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettingsFactory;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.UserDTO;
import com.sap.sse.gwt.ui.DataEntryDialog.DialogCallback;

/**
 * A view showing a list of components visualizing a race from the regattas announced by calls to {@link #fillRegattas(List)}.
 * The race selection is provided by a {@link RaceSelectionProvider} for which this is a {@link RaceSelectionChangeListener listener}.
 * {@link RaceIdentifier}-based race selection changes are converted to {@link RaceDTO} objects using the {@link #racesByIdentifier}
 * map maintained during {@link #fillRegattas(List)}. The race selection provider is expected to be single selection only.
 * 
 * @author Frank Mittag, Axel Uhl (d043530)
 *
 */
public class RaceBoardPanel extends SimplePanel implements RegattaDisplayer, RaceSelectionChangeListener {
    private final SailingServiceAsync sailingService;
    private final MediaServiceAsync mediaService;
    private final UserDTO user;
    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    private final RaceBoardViewConfiguration raceboardViewConfiguration;
    private String raceBoardName;
    
    /**
     * Updated upon each {@link #fillRegattas(List)}
     */
    private final Map<RaceIdentifier, RaceDTO> racesByIdentifier;
    
    /**
     * The offset when scrolling with the menu entry anchors (in the top right corner).
     */
    private int scrollOffset;

    private final List<ComponentViewer> componentViewers;
    private final FlowPanel componentControlsPanel;
    private final FlowPanel viewControlsPanel;
    private final FlowPanel toolbarPanel;
    private RaceTimePanel timePanel;
    private final Timer timer;
    private final RaceSelectionProvider raceSelectionProvider;
    private final UserAgentDetails userAgent;
    private final CompetitorSelectionModel competitorSelectionModel;
    private final TimeRangeWithZoomModel timeRangeWithZoomModel; 
    private final RegattaAndRaceIdentifier selectedRaceIdentifier;
    private final CompetitorsFilterSets competitorsFilterSets;

    private final LeaderboardPanel leaderboardPanel;
    private WindChart windChart;
    private MultiCompetitorRaceChart competitorChart;
    
    private CheckBox competitorsFilterCheckBox;
    private FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> lastActiveCompetitorFilterSet;
    
    /**
     * The component viewer in <code>ONESCREEN</code> view mode. <code>null</code> if in <code>CASCADE</code> view mode
     */
    private SideBySideComponentViewer leaderboardAndMapViewer;

    private final AsyncActionsExecutor asyncActionsExecutor;
    
    private final RaceTimesInfoProvider raceTimesInfoProvider;
    
    private final static String LOCAL_STORAGE_COMPETITORS_FILTER_SETS_KEY = "sailingAnalytics.raceBoard.competitorsFilterSets";

    public RaceBoardPanel(SailingServiceAsync sailingService, MediaServiceAsync mediaService, UserDTO theUser, Timer timer,
            RaceSelectionProvider theRaceSelectionProvider, String leaderboardName, String leaderboardGroupName,
            RaceBoardViewConfiguration raceboardViewConfiguration, ErrorReporter errorReporter, final StringMessages stringMessages, 
            UserAgentDetails userAgent, RaceTimesInfoProvider raceTimesInfoProvider) {
        this.sailingService = sailingService;
        this.mediaService = mediaService;
        this.user = theUser;
        this.stringMessages = stringMessages;
        this.raceboardViewConfiguration = raceboardViewConfiguration;
        this.raceSelectionProvider = theRaceSelectionProvider;
        this.raceTimesInfoProvider = raceTimesInfoProvider;
        this.errorReporter = errorReporter;
        this.userAgent = userAgent;
        this.timer = timer;
        this.scrollOffset = 0;        
        raceSelectionProvider.addRaceSelectionChangeListener(this);
        racesByIdentifier = new HashMap<RaceIdentifier, RaceDTO>();
        selectedRaceIdentifier = raceSelectionProvider.getSelectedRaces().iterator().next();
        this.setRaceBoardName(selectedRaceIdentifier.getRaceName());
        asyncActionsExecutor = new AsyncActionsExecutor();
        FlowPanel mainPanel = new FlowPanel();
        mainPanel.setSize("100%", "100%");
        setWidget(mainPanel);
        timeRangeWithZoomModel = new TimeRangeWithZoomModel();
        componentViewers = new ArrayList<ComponentViewer>();
        competitorSelectionModel = new CompetitorSelectionModel(/* hasMultiSelection */ true);
                
        toolbarPanel = new FlowPanel();
        toolbarPanel.setWidth("100%");
        componentControlsPanel = new FlowPanel();
        componentControlsPanel.addStyleName("raceBoardNavigation");
        toolbarPanel.add(componentControlsPanel);
        viewControlsPanel = new FlowPanel();
        viewControlsPanel.addStyleName("raceBoardControls");
        toolbarPanel.add(viewControlsPanel);
        
        leaderboardPanel = createLeaderboardPanel(leaderboardName, leaderboardGroupName);
        createOneScreenView(leaderboardName, leaderboardGroupName, mainPanel);                
        getElement().getStyle().setMarginLeft(12, Unit.PX);
        getElement().getStyle().setMarginRight(12, Unit.PX);
        
        CompetitorsFilterSets loadedCompetitorsFilterSets = loadCompetitorsFilterSets();
        if (loadedCompetitorsFilterSets != null) {
            competitorsFilterSets = loadedCompetitorsFilterSets;
            insertSelectedCompetitorsFilter(competitorsFilterSets);
        } else {
            competitorsFilterSets = createAndAddDefaultCompetitorsFilter();
            storeCompetitorsFilterSets(competitorsFilterSets);
        }
        
        // in case the URL configuration contains the name of a competitors filter set we try to activate it  
        if (raceboardViewConfiguration.getActiveCompetitorsFilterSetName() != null) {
            for (FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> filterSet: competitorsFilterSets.getFilterSets()) {
                if (filterSet.getName().equals(raceboardViewConfiguration.getActiveCompetitorsFilterSetName())) {
                    competitorsFilterSets.setActiveFilterSet(filterSet);
                    break;
                }
            }
        }
        /* TODO: Disabling automatic filter loading for now. Do NOT enable before
           there are tests especially for the pre-start phase!
        competitorSelectionModel.setCompetitorsFilterSet(competitorsFilterSets.getActiveFilterSet());
        updateCompetitorsFilterContexts(competitorsFilterSets);
        updateCompetitorsFilterControlState(competitorsFilterSets);*/

        timePanel = new RaceTimePanel(timer, timeRangeWithZoomModel, stringMessages, raceTimesInfoProvider, raceboardViewConfiguration.isCanReplayDuringLiveRaces());
        timeRangeWithZoomModel.addTimeZoomChangeListener(timePanel);
        raceTimesInfoProvider.addRaceTimesInfoProviderListener(timePanel);
        raceSelectionProvider.addRaceSelectionChangeListener(timePanel);
        timePanel.onRaceSelectionChange(raceSelectionProvider.getSelectedRaces());
    }
    
    private void createOneScreenView(String leaderboardName, String leaderboardGroupName, FlowPanel mainPanel) {
        // create the default leaderboard and select the right race
        RaceMap raceMap = new RaceMap(sailingService, asyncActionsExecutor, errorReporter, timer, competitorSelectionModel, stringMessages);
        raceTimesInfoProvider.addRaceTimesInfoProviderListener(raceMap);
        raceMap.onRaceSelectionChange(Collections.singletonList(selectedRaceIdentifier));
        List<Component<?>> components = new ArrayList<Component<?>>();
        competitorChart = new MultiCompetitorRaceChart(sailingService, asyncActionsExecutor, competitorSelectionModel, raceSelectionProvider,
                    timer, timeRangeWithZoomModel, stringMessages, errorReporter, true, true, leaderboardGroupName, leaderboardName);
        competitorChart.onRaceSelectionChange(raceSelectionProvider.getSelectedRaces());
        components.add(competitorChart);
        windChart = new WindChart(sailingService, raceSelectionProvider, timer, timeRangeWithZoomModel, new WindChartSettings(),
                stringMessages, asyncActionsExecutor, errorReporter, /* compactChart */ true);
        windChart.onRaceSelectionChange(raceSelectionProvider.getSelectedRaces());
        components.add(windChart);
        leaderboardAndMapViewer = new SideBySideComponentViewer(leaderboardPanel, raceMap, components);
        componentViewers.add(leaderboardAndMapViewer);
        for (ComponentViewer componentViewer : componentViewers) {
            mainPanel.add(componentViewer.getViewerWidget());
        }
        setLeaderboardVisible(getConfiguration().isShowLeaderboard());
        setWindChartVisible(getConfiguration().isShowWindChart());
        setCompetitorChartVisible(getConfiguration().isShowCompetitorsChart());
        
        addComponentToNavigationMenu(leaderboardAndMapViewer, leaderboardPanel, true);
        addComponentToNavigationMenu(leaderboardAndMapViewer, windChart,  true);
        addComponentToNavigationMenu(leaderboardAndMapViewer, competitorChart, true);
        addComponentToNavigationMenu(leaderboardAndMapViewer, raceMap, false);

        addCompetitorsFilterControl(viewControlsPanel);

        addMediaSelectorToNavigationMenu();   
    }
 
    private CompetitorsFilterSets createAndAddDefaultCompetitorsFilter() {
        CompetitorsFilterSets filterSets = new CompetitorsFilterSets();
        
        // 1. selected competitors filter
        insertSelectedCompetitorsFilter(filterSets);
        
        // 2. Top 50 competitors by race rank
        int maxRaceRank = 50;
        FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> topNRaceRankCompetitorsFilterSet = 
                new FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>>(stringMessages.topNCompetitorsByRaceRank(maxRaceRank));
        CompetitorRaceRankFilter raceRankFilter = new CompetitorRaceRankFilter();
        raceRankFilter.setOperator(new BinaryOperator<Integer>(BinaryOperator.Operators.LessThanEquals));
        raceRankFilter.setValue(maxRaceRank);
        topNRaceRankCompetitorsFilterSet.addFilter(raceRankFilter);
        filterSets.addFilterSet(topNRaceRankCompetitorsFilterSet);

        // 3. Top 50 competitors by total rank
        int maxTotalRank = 50;
        FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> topNTotalRankCompetitorsFilterSet =
                new FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>>(stringMessages.topNCompetitorsByTotalRank(maxTotalRank));
        CompetitorTotalRankFilter totalRankFilter = new CompetitorTotalRankFilter();
        totalRankFilter.setOperator(new BinaryOperator<Integer>(BinaryOperator.Operators.LessThanEquals));
        totalRankFilter.setValue(50);
        topNTotalRankCompetitorsFilterSet.addFilter(totalRankFilter);
        filterSets.addFilterSet(topNTotalRankCompetitorsFilterSet);
        
        // set default active filter
        filterSets.setActiveFilterSet(topNRaceRankCompetitorsFilterSet);
        
        return filterSets;
    }
    
    private void addMediaSelectorToNavigationMenu() {
        MediaSelector mediaSelector = new MediaSelector(selectedRaceIdentifier, raceTimesInfoProvider, timer, mediaService, stringMessages, errorReporter, this.user, getConfiguration().isAutoSelectMedia());
//      raceTimesInfoProvider.addRaceTimesInfoProviderListener(mediaSelector);
        timer.addPlayStateListener(mediaSelector);
        timer.addTimeListener(mediaSelector);
        mediaService.getMediaTracksForRace(selectedRaceIdentifier, mediaSelector);
        for (Widget widget : mediaSelector.widgets()) {
            componentControlsPanel.add(widget);
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
                        new ExplicitRaceColumnSelectionWithPreselectedRace(selectedRaceIdentifier));
        return new LeaderboardPanel(sailingService, asyncActionsExecutor, leaderBoardSettings, selectedRaceIdentifier,
                competitorSelectionModel, timer, leaderboardGroupName, leaderboardName, errorReporter, stringMessages,
                userAgent, /* showRaceDetails */ true, raceTimesInfoProvider, /* autoExpandLastRaceColumn */ false,
                /* don't adjust the timer's delay from the leaderboard; control it solely from the RaceTimesInfoProvider */ false);
    }

    private void updateCompetitorsFilterContexts(CompetitorsFilterSets filterSets) {
        for(FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> filterSet: filterSets.getFilterSets()) {
            for(Filter<CompetitorDTO> filter: filterSet.getFilters()) {
               if(filter instanceof LeaderboardFilterContext) {
                   ((LeaderboardFilterContext) filter).setLeaderboardFetcher(leaderboardPanel);
               }
               if(filter instanceof SelectedRaceFilterContext) {
                   ((SelectedRaceFilterContext) filter).setSelectedRace(selectedRaceIdentifier);
               }
               if(filter instanceof CompetitorSelectionProviderFilterContext) {
                   ((CompetitorSelectionProviderFilterContext) filter).setCompetitorSelectionProvider(competitorSelectionModel);
               }
            }
        }
    }

    private void updateCompetitorsFilterControlState(CompetitorsFilterSets filterSets) {
        String competitorsFilterTitle = stringMessages.competitorsFilter();
        FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> activeFilterSet = filterSets.getActiveFilterSet();
        if(activeFilterSet != null) {
            lastActiveCompetitorFilterSet = activeFilterSet;
        } else {
            if(filterSets.getFilterSets().size() == 0) {
                lastActiveCompetitorFilterSet = null;
            }
        }
        competitorsFilterCheckBox.setValue(activeFilterSet != null, false /* fireChangeValue*/);
        
        if(lastActiveCompetitorFilterSet != null) {
            competitorsFilterCheckBox.setText(competitorsFilterTitle + " (" + lastActiveCompetitorFilterSet.getName() + ")");
        } else {
            competitorsFilterCheckBox.setText(competitorsFilterTitle);            
        }
    }
    
    private void addCompetitorsFilterControl(Panel parentPanel) {
        String competitorsFilterTitle = stringMessages.competitorsFilter();
        competitorsFilterCheckBox = new CheckBox(competitorsFilterTitle);

        competitorsFilterCheckBox.getElement().getStyle().setFloat(Style.Float.LEFT);

        competitorsFilterCheckBox.setTitle(competitorsFilterTitle);
        competitorsFilterCheckBox.addStyleName("raceBoardNavigation-innerElement");

        competitorsFilterCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> newValue) {
                boolean isChecked = competitorsFilterCheckBox.getValue();
                if(isChecked) {
                    if(lastActiveCompetitorFilterSet != null) {
                        competitorsFilterSets.setActiveFilterSet(lastActiveCompetitorFilterSet);
                        competitorSelectionModel.setCompetitorsFilterSet(competitorsFilterSets.getActiveFilterSet());
                        updateCompetitorsFilterControlState(competitorsFilterSets);
                    } else {
                        showEditCompetitorsFiltersDialog();
                    }
                } else {
                    competitorsFilterSets.setActiveFilterSet(null);
                    competitorSelectionModel.setCompetitorsFilterSet(competitorsFilterSets.getActiveFilterSet());
                    updateCompetitorsFilterControlState(competitorsFilterSets);
                }
            }
        });

        parentPanel.add(competitorsFilterCheckBox);

        Button filterButton = new Button("");
        filterButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showEditCompetitorsFiltersDialog();
            }
        });
        filterButton.addStyleName("raceBoardNavigation-filterButton");
        filterButton.getElement().getStyle().setFloat(Style.Float.LEFT);
        filterButton.setTitle(competitorsFilterTitle);
        
        parentPanel.add(filterButton);
    }
    
    private void showEditCompetitorsFiltersDialog() {
        CompetitorsFilterSetsDialog competitorsFilterSetsDialog = new CompetitorsFilterSetsDialog(competitorsFilterSets,
                stringMessages, new DialogCallback<CompetitorsFilterSets>() {
            @Override
            public void ok(final CompetitorsFilterSets newCompetitorsFilterSets) {
                competitorsFilterSets.getFilterSets().clear();
                competitorsFilterSets.getFilterSets().addAll(newCompetitorsFilterSets.getFilterSets());
                competitorsFilterSets.setActiveFilterSet(newCompetitorsFilterSets.getActiveFilterSet());
                
                updateCompetitorsFilterContexts(newCompetitorsFilterSets);
                competitorSelectionModel.setCompetitorsFilterSet(newCompetitorsFilterSets.getActiveFilterSet());
                updateCompetitorsFilterControlState(newCompetitorsFilterSets);
                storeCompetitorsFilterSets(newCompetitorsFilterSets);
             }

            @Override
            public void cancel() { 
            }
        });
        
        competitorsFilterSetsDialog .show();
    }

    private void insertSelectedCompetitorsFilter(CompetitorsFilterSets filterSet) {
        // selected competitors filter
        FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> selectedCompetitorsFilterSet = 
                new FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>>(stringMessages.selectedCompetitors());
        selectedCompetitorsFilterSet.setEditable(false);
        SelectedCompetitorsFilter selectedCompetitorsFilter = new SelectedCompetitorsFilter();
        selectedCompetitorsFilter.setCompetitorSelectionProvider(competitorSelectionModel);
        selectedCompetitorsFilterSet.addFilter(selectedCompetitorsFilter);
        
        filterSet.addFilterSet(0, selectedCompetitorsFilterSet);
    }
    
    private CompetitorsFilterSets loadCompetitorsFilterSets() {
        CompetitorsFilterSets result = null;
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if(localStorage != null) {
            try {
                String jsonAsLocalStore = localStorage.getItem(LOCAL_STORAGE_COMPETITORS_FILTER_SETS_KEY);
                if(jsonAsLocalStore != null && !jsonAsLocalStore.isEmpty()) {
                    CompetitorsFilterSetsJsonDeSerializer deserializer = new CompetitorsFilterSetsJsonDeSerializer();
                    JSONValue value = JSONParser.parseStrict(jsonAsLocalStore);
                    if(value.isObject() != null) {
                        result = deserializer.deserialize((JSONObject) value);
                    }
                }
            } catch (Exception e) {
                // exception during loading of competitor filters from local storage
            }
        }
        return result;
    }

    private void storeCompetitorsFilterSets(CompetitorsFilterSets newCompetitorsFilterSets) {
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if(localStorage != null) {
            // delete old value
            localStorage.removeItem(LOCAL_STORAGE_COMPETITORS_FILTER_SETS_KEY);
            
            // store the competiors filter set 
            CompetitorsFilterSetsJsonDeSerializer serializer = new CompetitorsFilterSetsJsonDeSerializer();
            JSONObject jsonObject = serializer.serialize(newCompetitorsFilterSets);
            localStorage.setItem(LOCAL_STORAGE_COMPETITORS_FILTER_SETS_KEY, jsonObject.toString());
        }
    }
    
    private <SettingsType> void addComponentToNavigationMenu(final ComponentViewer componentViewer,
            final Component<SettingsType> component, boolean isToogleCheckboxEnabled) {
        final CheckBox checkBox = new CheckBox(component.getLocalizedShortName());
        checkBox.getElement().getStyle().setFloat(Style.Float.LEFT);

        checkBox.setEnabled(isToogleCheckboxEnabled);
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
                    ((TimeListener) component).timeChanged(timer.getTime(), null);
                }
            }
        });

        componentControlsPanel.add(checkBox);

        if (component.hasSettings()) {
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
            
            componentControlsPanel.add(settingsButton);
        }
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
        switch (getConfiguration().getViewMode()) {
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
        switch (getConfiguration().getViewMode()) {
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
        switch (getConfiguration().getViewMode()) {
        case ONESCREEN:
            setComponentVisible(leaderboardAndMapViewer, competitorChart, visible);
            break;
        }
    }
    
    public Panel getToolbarPanel() {
        return toolbarPanel; 
    }

    public Panel getTimePanel() {
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

    public RaceBoardViewConfiguration getConfiguration() {
        return raceboardViewConfiguration;
    }
    
    public FlowPanel getComponentControlsPanel() {
        return componentControlsPanel;
    }
}

