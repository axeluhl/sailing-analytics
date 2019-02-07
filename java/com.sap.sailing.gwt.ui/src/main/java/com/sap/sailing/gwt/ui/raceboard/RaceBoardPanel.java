package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.domain.common.dto.TagDTO;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.gwt.common.authentication.SailingAuthenticationEntryPointLinkFactory;
import com.sap.sailing.gwt.settings.client.leaderboard.SingleRaceLeaderboardSettings;
import com.sap.sailing.gwt.settings.client.raceboard.RaceBoardPerspectiveOwnSettings;
import com.sap.sailing.gwt.ui.client.CompetitorColorProvider;
import com.sap.sailing.gwt.ui.client.CompetitorColorProviderImpl;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.EntryPointLinkFactory;
import com.sap.sailing.gwt.ui.client.FlagImageResolverImpl;
import com.sap.sailing.gwt.ui.client.LeaderboardUpdateListener;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.RaceCompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.RaceCompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.RaceTimePanel;
import com.sap.sailing.gwt.ui.client.RaceTimePanelLifecycle;
import com.sap.sailing.gwt.ui.client.RaceTimePanelSettings;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.media.MediaPlayerLifecycle;
import com.sap.sailing.gwt.ui.client.media.MediaPlayerManager.PlayerChangeListener;
import com.sap.sailing.gwt.ui.client.media.MediaPlayerManagerComponent;
import com.sap.sailing.gwt.ui.client.media.MediaPlayerSettings;
import com.sap.sailing.gwt.ui.client.media.PopupPositionProvider;
import com.sap.sailing.gwt.ui.client.shared.charts.EditMarkPassingsPanel;
import com.sap.sailing.gwt.ui.client.shared.charts.EditMarkPositionPanel;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorRaceChart;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorRaceChartLifecycle;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorRaceChartSettings;
import com.sap.sailing.gwt.ui.client.shared.charts.WindChart;
import com.sap.sailing.gwt.ui.client.shared.charts.WindChartLifecycle;
import com.sap.sailing.gwt.ui.client.shared.charts.WindChartSettings;
import com.sap.sailing.gwt.ui.client.shared.filter.FilterWithUI;
import com.sap.sailing.gwt.ui.client.shared.filter.LeaderboardFetcher;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceCompetitorSet;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapLifecycle;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapResources;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapSettings;
import com.sap.sailing.gwt.ui.client.shared.racemap.maneuver.ManeuverTableLifecycle;
import com.sap.sailing.gwt.ui.client.shared.racemap.maneuver.ManeuverTablePanel;
import com.sap.sailing.gwt.ui.client.shared.racemap.maneuver.ManeuverTableSettings;
import com.sap.sailing.gwt.ui.leaderboard.ClassicLeaderboardStyle;
import com.sap.sailing.gwt.ui.leaderboard.CompetitorFilterPanel;
import com.sap.sailing.gwt.ui.leaderboard.SingleRaceLeaderboardPanel;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardResources.RaceBoardMainCss;
import com.sap.sailing.gwt.ui.raceboard.tagging.TaggingPanel;
import com.sap.sailing.gwt.ui.shared.RaceWithCompetitorsAndBoatsDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTOWithSecurity;
import com.sap.sse.common.Distance;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.FilterSet;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.controls.slider.TimeSlider.BarOverlay;
import com.sap.sse.gwt.client.panels.ResizableFlowPanel;
import com.sap.sse.gwt.client.player.TimeRangeWithZoomModel;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialog;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.AbstractPerspectiveComposite;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;
import com.sap.sse.security.ui.authentication.generic.GenericAuthentication;
import com.sap.sse.security.ui.authentication.view.AuthenticationMenuView;
import com.sap.sse.security.ui.authentication.view.AuthenticationMenuViewImpl;
import com.sap.sse.security.ui.authentication.view.FlyoutAuthenticationView;
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
public class RaceBoardPanel
        extends AbstractPerspectiveComposite<RaceBoardPerspectiveLifecycle, RaceBoardPerspectiveOwnSettings>
        implements LeaderboardUpdateListener, PopupPositionProvider, RequiresResize {
    private final SailingServiceAsync sailingService;
    private final MediaServiceAsync mediaService;
    private final UUID eventId;
    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    private String raceBoardName;
        
    private final RaceTimePanel racetimePanel;
    private final Timer timer;
    private final UserAgentDetails userAgent;
    private final RaceCompetitorSelectionProvider competitorSelectionProvider;
    private final TimeRangeWithZoomModel timeRangeWithZoomModel; 
    private final RegattaAndRaceIdentifier selectedRaceIdentifier;

    private final String leaderboardName;
    private final SingleRaceLeaderboardPanel leaderboardPanel;
    private WindChart windChart;
    private MultiCompetitorRaceChart competitorChart;
    private MediaPlayerManagerComponent mediaPlayerManagerComponent;
    private EditMarkPassingsPanel editMarkPassingPanel;
    private EditMarkPositionPanel editMarkPositionPanel;
    
    private final TaggingPanel taggingPanel;
    
    private final DockLayoutPanel dockPanel;
    private final ResizableFlowPanel timePanelWrapper;
    private final static int TIMEPANEL_COLLAPSED_HEIGHT = 67;
    private final static int TIMEPANEL_EXPANDED_HEIGHT = 96;
    
    /**
     * The component viewer
     */
    private SideBySideComponentViewer mapViewer;

    private final AsyncActionsExecutor asyncActionsExecutor;
    
    private final RaceTimesInfoProvider raceTimesInfoProvider;
    private final RaceMap raceMap;
    
    private final FlowPanel raceInformationHeader;
    private final FlowPanel regattaAndRaceTimeInformationHeader;
    private final AuthenticationMenuView userManagementMenuView;
    private boolean currentRaceHasBeenSelectedOnce;
    
    private final RaceBoardResources raceBoardResources = RaceBoardResources.INSTANCE; 
    private final RaceBoardMainCss mainCss = raceBoardResources.mainCss();
    private final QuickRanksDTOFromLeaderboardDTOProvider quickRanksDTOProvider;
    private ManeuverTablePanel maneuverTablePanel;

    private static final RaceMapResources raceMapResources = GWT.create(RaceMapResources.class);
    
    /**
     * @param eventId
     *            an optional event that can be used for "back"-navigation in case the race board shows a race in the
     *            context of an event; may be <code>null</code>.
     * @param isScreenLargeEnoughToOfferChartSupport
     *            if the screen is large enough to display charts such as the competitor chart or the wind chart, a
     *            padding is provided for the RaceTimePanel that aligns its right border with that of the charts, and
     *            the charts are created. This decision is made once on startup in the {@link RaceBoardEntryPoint}
     *            class.
     * @param showChartMarkEditMediaButtonsAndVideo
     *            if <code>true</code> charts, such as the competitor chart or the wind chart, (as well as edit mark
     *            panels and manage media buttons) are shown and a padding is provided for the RaceTimePanel that aligns
     *            its right border with that of the chart. Otherwise those components will be hidden.
     * @param availableDetailTypes
     *            A list of all Detailtypes, that will be offered in the Settingsdialog. Can be used to hide settings no
     *            data exists for, eg Bravo, Expdition ect.
     */
    public RaceBoardPanel(Component<?> parent,
            ComponentContext<PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings>> componentContext,
            RaceBoardPerspectiveLifecycle lifecycle,
            PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> settings,
            SailingServiceAsync sailingService, MediaServiceAsync mediaService, UserService userService,
            AsyncActionsExecutor asyncActionsExecutor, Map<CompetitorDTO, BoatDTO> competitorsAndTheirBoats,
            Timer timer, RegattaAndRaceIdentifier selectedRaceIdentifier, String leaderboardName,
            String leaderboardGroupName, UUID eventId, ErrorReporter errorReporter, final StringMessages stringMessages,
            UserAgentDetails userAgent, RaceTimesInfoProvider raceTimesInfoProvider,
            boolean showChartMarkEditMediaButtonsAndVideo, boolean showHeaderPanel,
            Iterable<DetailType> availableDetailTypes, StrippedLeaderboardDTOWithSecurity leaderboardDTO,
            RaceWithCompetitorsAndBoatsDTO raceDTO) {
        super(parent, componentContext, lifecycle, settings);
        this.sailingService = sailingService;
        this.mediaService = mediaService;
        this.stringMessages = stringMessages;
        this.raceTimesInfoProvider = raceTimesInfoProvider;
        this.errorReporter = errorReporter;
        this.userAgent = userAgent;
        this.timer = timer;
        this.eventId = eventId;
        this.currentRaceHasBeenSelectedOnce = false;
        this.leaderboardName = leaderboardName;
        this.selectedRaceIdentifier = selectedRaceIdentifier;
        this.setRaceBoardName(selectedRaceIdentifier.getRaceName());
        this.asyncActionsExecutor = asyncActionsExecutor;
        FlowPanel mainPanel = new ResizableFlowPanel();
        mainPanel.setSize("100%", "100%");
        raceInformationHeader = new FlowPanel();
        raceInformationHeader.setStyleName("RegattaRaceInformation-Header");
        regattaAndRaceTimeInformationHeader = new FlowPanel();
        regattaAndRaceTimeInformationHeader.setStyleName("RegattaAndRaceTime-Header");
        this.userManagementMenuView = new AuthenticationMenuViewImpl(new Anchor(), mainCss.usermanagement_loggedin(), mainCss.usermanagement_open());
        this.userManagementMenuView.asWidget().setStyleName(mainCss.usermanagement_icon());
        timeRangeWithZoomModel = new TimeRangeWithZoomModel();

        final CompetitorColorProvider colorProvider = new CompetitorColorProviderImpl(selectedRaceIdentifier, competitorsAndTheirBoats);
        competitorSelectionProvider = new RaceCompetitorSelectionModel(/* hasMultiSelection */ true, colorProvider, competitorsAndTheirBoats);
                
        raceMapResources.raceMapStyle().ensureInjected();
        RaceMapLifecycle raceMapLifecycle = lifecycle.getRaceMapLifecycle();
        RaceMapSettings defaultRaceMapSettings = settings.findSettingsByComponentId(raceMapLifecycle.getComponentId());

        RaceTimePanelLifecycle raceTimePanelLifecycle = lifecycle.getRaceTimePanelLifecycle();
        RaceTimePanelSettings raceTimePanelSettings = settings
                .findSettingsByComponentId(raceTimePanelLifecycle.getComponentId());
        final RaceCompetitorSet raceCompetitorSet = new RaceCompetitorSet(competitorSelectionProvider);
        quickRanksDTOProvider = new QuickRanksDTOFromLeaderboardDTOProvider(raceCompetitorSet, selectedRaceIdentifier);
        raceMap = new RaceMap(this, componentContext, raceMapLifecycle, defaultRaceMapSettings, sailingService, asyncActionsExecutor,
                errorReporter, timer,
                competitorSelectionProvider, raceCompetitorSet, stringMessages, selectedRaceIdentifier, 
                raceMapResources, /* showHeaderPanel */ true, quickRanksDTOProvider, this::showInWindChart) {
            private static final String INDENT_SMALL_CONTROL_STYLE = "indentsmall";
            private static final String INDENT_BIG_CONTROL_STYLE = "indentbig";
            @Override
            public void onResize() {
                super.onResize();
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        // Show/hide the leaderboard panels toggle button text based on the race map height
                        mapViewer.setLeftComponentToggleButtonTextVisibilityAndDraggerPosition(raceMap.getOffsetHeight() > 400);
                        mapViewer.setRightComponentToggleButtonTextVisibilityAndDraggerPosition(raceMap.getOffsetHeight() > 400);
                    }
                });
            }
            
            @Override
            protected String getLeftControlsIndentStyle() {
                // Calculate style name for left control indent based on race map height an leaderboard panel visibility
                if (raceMap.getOffsetHeight() <= 300) {
                    return INDENT_BIG_CONTROL_STYLE;
                }
                if (leaderboardPanel.isVisible() && raceMap.getOffsetHeight() <= 500) {
                    return INDENT_SMALL_CONTROL_STYLE;
                }
                return super.getLeftControlsIndentStyle();
            }   
        };
        // now that the raceMap field has been initialized, check whether the buoy zone radius shall be looked up from
        // the regatta model on the server:
        if (defaultRaceMapSettings.isBuoyZoneRadiusDefaultValue()) {
            sailingService.getRegattaByName(selectedRaceIdentifier.getRegattaName(), new AsyncCallback<RegattaDTO>() {
                @Override
                public void onSuccess(RegattaDTO regattaDTO) {
                    Distance buoyZoneRadius = regattaDTO.getCalculatedBuoyZoneRadius();
                    RaceMapSettings existingMapSettings = raceMap.getSettings();
                    if (!Util.equalsWithNull(buoyZoneRadius, existingMapSettings.getBuoyZoneRadius())) {
                        final RaceMapSettings newRaceMapSettings = RaceMapSettings.createSettingsWithNewBuoyZoneRadius(existingMapSettings, buoyZoneRadius);
                        raceMap.updateSettings(newRaceMapSettings);
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                }
            });
        }

        CompetitorFilterPanel competitorSearchTextBox = new CompetitorFilterPanel(competitorSelectionProvider, stringMessages, raceMap,
                new LeaderboardFetcher() {
                    @Override
                    public LeaderboardDTO getLeaderboard() {
                        return leaderboardPanel.getLeaderboard();
                    }
                }, selectedRaceIdentifier);
        raceMap.getLeftHeaderPanel().add(raceInformationHeader);
        raceMap.getRightHeaderPanel().add(regattaAndRaceTimeInformationHeader);
        raceMap.getRightHeaderPanel().add(userManagementMenuView);
        addChildComponent(raceMap);
        
        // add panel for tagging functionality, hidden if no URL parameter "tag" is passed 
        final String sharedTagURLParameter = settings.getPerspectiveOwnSettings().getJumpToTag();
        String sharedTagTitle = null;
        TimePoint sharedTagTimePoint = null;
        boolean showTaggingPanel = false;
        if (sharedTagURLParameter != null) {
            showTaggingPanel = true;
            int indexOfSeperator = sharedTagURLParameter.indexOf(",");
            if (indexOfSeperator != -1) {
                try {
                    sharedTagTimePoint = new MillisecondsTimePoint(Long.parseLong(sharedTagURLParameter.substring(0, indexOfSeperator)));
                    sharedTagTitle = sharedTagURLParameter.substring(indexOfSeperator + 1, sharedTagURLParameter.length());
                } catch(NumberFormatException nfe) {
                    GWT.log("Problem extracting tag time point from URL parameter "+TagDTO.TAG_URL_PARAMETER, nfe);
                }
            }
        }
        taggingPanel = new TaggingPanel(parent, componentContext, stringMessages, sailingService, userService, timer,
                raceTimesInfoProvider, sharedTagTimePoint, sharedTagTitle, leaderboardDTO);
        addChildComponent(taggingPanel);
        taggingPanel.setVisible(showTaggingPanel);
        
        // Determine if the screen is large enough to initially display the leaderboard panel on the left side of the
        // map based on the initial screen width. Afterwards, the leaderboard panel visibility can be toggled as usual.
        boolean isScreenLargeEnoughToInitiallyDisplayLeaderboard = Document.get().getClientWidth() >= 1024;
        leaderboardPanel = createLeaderboardPanel(lifecycle, settings, leaderboardName, leaderboardGroupName,
                competitorSearchTextBox, availableDetailTypes);
        addChildComponent(leaderboardPanel);
        leaderboardPanel.addVisibilityListener(visible->{
            quickRanksDTOProvider.setLeaderboardNotCurrentlyUpdating(!visible);
        });

        leaderboardPanel.setTitle(stringMessages.leaderboard());
        leaderboardPanel.getElement().getStyle().setMarginLeft(6, Unit.PX);
        leaderboardPanel.getElement().getStyle().setMarginTop(10, Unit.PX);
        createOneScreenView(lifecycle, settings, leaderboardName, leaderboardGroupName, eventId, mainPanel,
                isScreenLargeEnoughToInitiallyDisplayLeaderboard,
                raceMap, userService, showChartMarkEditMediaButtonsAndVideo, leaderboardDTO, raceDTO); // initializes
                                                                                                       // the raceMap
                                                                                              // field
        leaderboardPanel.addLeaderboardUpdateListener(this);
        
        // in case the URL configuration contains the name of a competitors filter set we try to activate it
        // FIXME the competitorsFilterSets has now moved to CompetitorSearchTextBox (which should probably be renamed); pass on the parameters to the LeaderboardPanel and see what it does with it
        if (getPerspectiveSettings().getActiveCompetitorsFilterSetName() != null) {
            for (FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> filterSet : competitorSearchTextBox.getCompetitorsFilterSets().getFilterSets()) {
                if (filterSet.getName().equals(getPerspectiveSettings().getActiveCompetitorsFilterSetName())) {
                    competitorSearchTextBox.getCompetitorsFilterSets().setActiveFilterSet(filterSet);
                    break;
                }
            }
        }
        racetimePanel = new RaceTimePanel(this, componentContext, raceTimePanelLifecycle, userService, timer,
                timeRangeWithZoomModel,
                stringMessages, raceTimesInfoProvider, getPerspectiveSettings().isCanReplayDuringLiveRaces(),
                showChartMarkEditMediaButtonsAndVideo, selectedRaceIdentifier,
                getPerspectiveSettings().getInitialDurationAfterRaceStartInReplay());
        racetimePanel.updateSettings(raceTimePanelSettings);
        timeRangeWithZoomModel.addTimeZoomChangeListener(racetimePanel);
        raceTimesInfoProvider.addRaceTimesInfoProviderListener(racetimePanel);
        this.timePanelWrapper = createTimePanelLayoutWrapper();
        boolean advanceTimePanelEnabled = true;
        if (advanceTimePanelEnabled) {
            manageTimePanelToggleButton(advanceTimePanelEnabled);
        }
        dockPanel = new DockLayoutPanel(Unit.PX);
        dockPanel.addSouth(timePanelWrapper, TIMEPANEL_COLLAPSED_HEIGHT);
        dockPanel.add(mainPanel);
        dockPanel.addStyleName("dockLayoutPanel");
        initWidget(dockPanel);
    }
    
    /**
     * @param event
     *            an optional event; may be <code>null</code> or else can be used to show some context information.
     * @param showChartMarkEditMediaButtonsAndVideo 
     * @param isScreenLargeEnoughToOfferChartSupport
     *            if the screen is large enough to display charts such as the competitor chart or the wind chart, a
     *            padding is provided for the RaceTimePanel that aligns its right border with that of the charts, and
     *            the charts are created.
     */
    private void createOneScreenView(RaceBoardPerspectiveLifecycle lifecycle,
            PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> settings, String leaderboardName,
            String leaderboardGroupName, UUID event,
            FlowPanel mainPanel, boolean isScreenLargeEnoughToInitiallyDisplayLeaderboard, RaceMap raceMap,
            UserService userService, boolean showChartMarkEditMediaButtonsAndVideo,
            StrippedLeaderboardDTOWithSecurity leaderboard, RaceWithCompetitorsAndBoatsDTO raceDTO) {
        MediaPlayerLifecycle mediaPlayerLifecycle = getPerspectiveLifecycle().getMediaPlayerLifecycle();
        MediaPlayerSettings mediaPlayerSettings = settings
                .findSettingsByComponentId(mediaPlayerLifecycle.getComponentId());
        WindChartLifecycle windChartLifecycle = getPerspectiveLifecycle().getWindChartLifecycle();
        WindChartSettings windChartSettings = settings.findSettingsByComponentId(windChartLifecycle.getComponentId());
        ManeuverTableLifecycle maneuverTableLifecycle = getPerspectiveLifecycle().getManeuverTable();
        ManeuverTableSettings maneuverTableSettings = settings.findSettingsByComponentId(maneuverTableLifecycle.getComponentId());
        MultiCompetitorRaceChartLifecycle multiCompetitorRaceChartLifecycle = getPerspectiveLifecycle().getMultiCompetitorRaceChartLifecycle();
        MultiCompetitorRaceChartSettings multiCompetitorRaceChartSettings = settings
                .findSettingsByComponentId(multiCompetitorRaceChartLifecycle.getComponentId());
        // create the default leaderboard and select the right race
        raceTimesInfoProvider.addRaceTimesInfoProviderListener(raceMap);
        List<Component<?>> componentsForSideBySideViewer = new ArrayList<Component<?>>();
        if (showChartMarkEditMediaButtonsAndVideo) {
            competitorChart = new MultiCompetitorRaceChart(this, getComponentContext(),
                    multiCompetitorRaceChartLifecycle,
                    sailingService,
                    asyncActionsExecutor,
                    competitorSelectionProvider, selectedRaceIdentifier, timer, timeRangeWithZoomModel, stringMessages,
                    errorReporter, true, true, leaderboardGroupName, leaderboardName);
            competitorChart.setVisible(false);
            competitorChart.updateSettings(multiCompetitorRaceChartSettings);
            new SliceRaceHandler(sailingService, userService, errorReporter, competitorChart, selectedRaceIdentifier,
                    leaderboardGroupName, leaderboardName, event, leaderboard, raceDTO);
            componentsForSideBySideViewer.add(competitorChart);
            windChart = new WindChart(this, getComponentContext(), windChartLifecycle, sailingService,
                    selectedRaceIdentifier, timer,
                    timeRangeWithZoomModel,
                    windChartSettings, stringMessages, asyncActionsExecutor, errorReporter, /* compactChart */
                    true);
            windChart.setVisible(false);
            componentsForSideBySideViewer.add(windChart);
            
        }
        maneuverTablePanel = new ManeuverTablePanel(this, getComponentContext(), sailingService, asyncActionsExecutor,
                selectedRaceIdentifier, stringMessages, competitorSelectionProvider, errorReporter, timer,
                maneuverTableSettings, timeRangeWithZoomModel, new ClassicLeaderboardStyle(), userService);
        maneuverTablePanel.getEntryWidget().setTitle(stringMessages.maneuverTable());
        if (showChartMarkEditMediaButtonsAndVideo) {
            componentsForSideBySideViewer.add(maneuverTablePanel);
        }
        editMarkPassingPanel = new EditMarkPassingsPanel(this, getComponentContext(), sailingService,
                selectedRaceIdentifier,
                stringMessages,
                competitorSelectionProvider, errorReporter, timer);
        if (showChartMarkEditMediaButtonsAndVideo) {
            editMarkPassingPanel.setLeaderboard(leaderboardPanel.getLeaderboard());
            editMarkPassingPanel.getEntryWidget().setTitle(stringMessages.editMarkPassings());
            componentsForSideBySideViewer.add(editMarkPassingPanel);
        }
        editMarkPositionPanel = new EditMarkPositionPanel(this, getComponentContext(), raceMap, leaderboardPanel,
                selectedRaceIdentifier,
                leaderboardName, stringMessages, sailingService, timer, timeRangeWithZoomModel,
                asyncActionsExecutor, errorReporter);
        if (showChartMarkEditMediaButtonsAndVideo) {
            editMarkPositionPanel.setLeaderboard(leaderboardPanel.getLeaderboard());
            componentsForSideBySideViewer.add(editMarkPositionPanel);
        }
        mediaPlayerManagerComponent = new MediaPlayerManagerComponent(this, getComponentContext(), mediaPlayerLifecycle,
                selectedRaceIdentifier, raceTimesInfoProvider, timer, mediaService, userService, stringMessages,
                errorReporter, userAgent, this, mediaPlayerSettings);
        mapViewer = new SideBySideComponentViewer(leaderboardPanel, raceMap, taggingPanel, mediaPlayerManagerComponent,
                componentsForSideBySideViewer, stringMessages, userService, editMarkPassingPanel, editMarkPositionPanel, maneuverTablePanel);
        
        mediaPlayerManagerComponent.addPlayerChangeListener(new PlayerChangeListener() {
            
            @Override
            public void notifyStateChange() {
                updateRaceTimePanelOverlay();
            }
        });
        
        for (Component<? extends Settings> component : componentsForSideBySideViewer) {
            addChildComponent(component);
        }
        this.setupUserManagementControlPanel(userService);
        mainPanel.add(mapViewer.getViewerWidget());
        boolean showLeaderboard = getPerspectiveSettings().isShowLeaderboard() && isScreenLargeEnoughToInitiallyDisplayLeaderboard;
        setLeaderboardVisible(showLeaderboard);
        if (showChartMarkEditMediaButtonsAndVideo) {
            setWindChartVisible(getPerspectiveSettings().isShowWindChart());
            setCompetitorChartVisible(getPerspectiveSettings().isShowCompetitorsChart());
        }
        // make sure to load leaderboard data for filtering to work
        if (!showLeaderboard) {
            leaderboardPanel.setVisible(true);
            leaderboardPanel.setVisible(false);
        }
    }
    
    protected void updateRaceTimePanelOverlay() {
        ArrayList<BarOverlay> overlays = new ArrayList<BarOverlay>();
        Set<MediaTrack> videoPlaying = mediaPlayerManagerComponent.getPlayingVideoTracks();
        Set<MediaTrack> audioPlaying = mediaPlayerManagerComponent.getPlayingAudioTrack();
        for (MediaTrack track : mediaPlayerManagerComponent.getAssignedMediaTracks()) {
            double start = track.startTime.asMillis();
            // do not show bars for very short videos but show for live streaming ones
            if (track.duration == null || track.duration.asMinutes() > 1) {
                TimePoint endTp = track.deriveEndTime();
                double end;
                if (endTp == null) {
                    end = Double.MAX_VALUE;
                } else {
                    end = endTp.asMillis();
                }
                final boolean isPlaying = videoPlaying.contains(track) || audioPlaying.contains(track);
                overlays.add(new BarOverlay(start, end, isPlaying,
                        track.title));
            }
        }
        racetimePanel.setBarOverlays(overlays);
    }

    private void setupUserManagementControlPanel(UserService userService) {
        mainCss.ensureInjected();
        final FlyoutAuthenticationView display = new RaceBoardAuthenticationView();
        final GenericAuthentication genericAuthentication = new GenericAuthentication(userService, userManagementMenuView, display, 
                SailingAuthenticationEntryPointLinkFactory.INSTANCE, raceBoardResources);
        new RaceBoardLoginHintPopup(genericAuthentication.getAuthenticationManager());
    }

    @SuppressWarnings("unused")
    private <SettingsType extends AbstractSettings> void addSettingsMenuItem(MenuBar settingsMenu, final Component<SettingsType> component) {
        if (component.hasSettings()) {
            MenuItem settingsMenuItem = settingsMenu.addItem(component.getLocalizedShortName(), new Command() {
                public void execute() {
                    new SettingsDialog<SettingsType>(component, stringMessages).show();
                  }
            });
        }
    }
    
    private SingleRaceLeaderboardPanel createLeaderboardPanel(RaceBoardPerspectiveLifecycle lifecycle,
            PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> settings, String leaderboardName,
            String leaderboardGroupName, CompetitorFilterPanel competitorSearchTextBox,
            Iterable<DetailType> availableDetailTypes) {
        SingleRaceLeaderboardPanelLifecycle leaderboardPanelLifecycle = getPerspectiveLifecycle().getLeaderboardPanelLifecycle();
        SingleRaceLeaderboardSettings leaderboardSettings = settings
                .findSettingsByComponentId(leaderboardPanelLifecycle.getComponentId());
        return new SingleRaceLeaderboardPanel(this, getComponentContext(), sailingService, asyncActionsExecutor,
                leaderboardSettings, selectedRaceIdentifier != null, selectedRaceIdentifier,
                competitorSelectionProvider, timer, leaderboardGroupName, leaderboardName, errorReporter, stringMessages,
                /* showRaceDetails */ true, competitorSearchTextBox,
                /* showSelectionCheckbox */ true, raceTimesInfoProvider, /* autoExpandLastRaceColumn */ false,
                /* don't adjust the timer's delay from the leaderboard; control it solely from the RaceTimesInfoProvider */ false,
                /* autoApplyTopNFilter */ false, /* showCompetitorFilterStatus */ false, /* enableSyncScroller */ false,
                new ClassicLeaderboardStyle(), FlagImageResolverImpl.get(), availableDetailTypes);
    }

    private void setComponentVisible(SideBySideComponentViewer componentViewer, Component<?> component, boolean visible) {
        component.setVisible(visible);      
        componentViewer.forceLayout();
    }
    
    SingleRaceLeaderboardPanel getLeaderboardPanel() {
        return leaderboardPanel;
    }
    
    MultiCompetitorRaceChart getCompetitorChart() {
        return competitorChart;
    }
    
    WindChart getWindChart() {
        return windChart;
    }
    
    RaceTimePanel getRaceTimePanel() {
        return racetimePanel;
    }
    
    Timer getTimer() {
        return timer;
    }
    
    RaceMap getMap() {
        return raceMap;
    }
    
    RegattaAndRaceIdentifier getSelectedRaceIdentifier() {
        return selectedRaceIdentifier;
    }
    
    CompetitorSelectionProvider getCompetitorSelectionProvider() {
        return competitorSelectionProvider;
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
        setComponentVisible(mapViewer, leaderboardPanel, visible);
    }
    
    /**
     * Sets the collapsable panel for the tagging open or close, if in <code>CASCADE</code> view mode.<br />
     * Displays or hides the tagging panel, if in <code>ONESCREEN</code> view mode.<br /><br />
     * 
     * The race board should be completely rendered before this method is called, or a few exceptions could be thrown.
     * 
     * @param visible <code>true</code> if the leaderboard shall be open/visible
     */
    public void setTaggingPanelVisible(boolean visible) {
        setComponentVisible(mapViewer, taggingPanel, visible);
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
        setComponentVisible(mapViewer, windChart, visible);
    }
    
    public void showInWindChart(WindSource windprovider) {
        setComponentVisible(mapViewer, windChart, true);
        windChart.showProvider(windprovider);
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
        setComponentVisible(mapViewer, competitorChart, visible);
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
    public void updatedLeaderboard(LeaderboardDTO leaderboard) {
        if (editMarkPassingPanel != null) {
            editMarkPassingPanel.setLeaderboard(leaderboard);
        }
        if (editMarkPositionPanel != null) {
            editMarkPositionPanel.setLeaderboard(leaderboard);
        }
        quickRanksDTOProvider.updateQuickRanks(leaderboard);
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
                fleetForRaceName = (seriesName.isEmpty() ? "" : " - ") + fleetForRaceName;
            }
            final Label raceNameLabel = new Label(stringMessages.race() + " " + raceColumn.getRaceColumnName());
            raceNameLabel.setStyleName("RaceName-Label");
            final Label raceAdditionalInformationLabel = new Label(seriesName + fleetForRaceName);
            raceAdditionalInformationLabel.setStyleName("RaceSeriesAndFleet-Label");
            raceInformationHeader.clear();
            raceInformationHeader.add(raceNameLabel);
            raceInformationHeader.add(raceAdditionalInformationLabel);
            final Anchor regattaNameAnchor = new Anchor(raceIdentifier.getRegattaName());
            regattaNameAnchor.setTitle(raceIdentifier.getRegattaName());
            if (eventId != null) {
                String link = EntryPointLinkFactory.createRacesTabLink(eventId.toString(), leaderboardName);
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
            Label raceTimeLabel = computeRaceInformation(raceColumn, fleet);
            raceTimeLabel.setStyleName("RaceTime-Label");
            regattaAndRaceTimeInformationHeader.clear();
            regattaAndRaceTimeInformationHeader.add(regattaNameAnchor);
            regattaAndRaceTimeInformationHeader.add(raceTimeLabel);
            currentRaceHasBeenSelectedOnce = true;
            taggingPanel.updateRace(leaderboardName, raceColumn, fleet);
        }
    }

    @Override
    public UIObject getXPositionUiObject() {
        return racetimePanel;
    }

    @Override
    public UIObject getYPositionUiObject() {
        return racetimePanel;
    }

    private Label computeRaceInformation(RaceColumnDTO raceColumn, FleetDTO fleet) {
        final Date startDate = raceColumn.getStartDate(fleet);
        Label raceInformationLabel = new Label();
        raceInformationLabel.setStyleName("Race-Time-Label");
        if (startDate != null) {
            DateTimeFormat formatter = DateTimeFormat.getFormat("E d/M/y");
            raceInformationLabel.setText(formatter.format(startDate));
        }
        return raceInformationLabel;
    }
    
    @Override
    public Widget getEntryWidget() {
        return this;
    }

    @Override
    public String getDependentCssClassName() {
        return "";
    }

    private void manageTimePanelToggleButton(boolean advanceTimePanelEnabled) {
        final Button toggleButton = getRaceTimePanel().getAdvancedToggleButton();
        if (advanceTimePanelEnabled) {
            toggleButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    boolean advancedModeShown = getRaceTimePanel().toggleAdvancedMode();
                    if (advancedModeShown) {
                        dockPanel.setWidgetSize(timePanelWrapper, TIMEPANEL_EXPANDED_HEIGHT);
                        toggleButton.removeStyleDependentName("Closed");
                        toggleButton.addStyleDependentName("Open");
                    } else {
                        dockPanel.setWidgetSize(timePanelWrapper, TIMEPANEL_COLLAPSED_HEIGHT);
                        toggleButton.addStyleDependentName("Closed");
                        toggleButton.removeStyleDependentName("Open");
                    }
                }
            });
        } else {
            toggleButton.setVisible(false);
        }
    }
    
    private ResizableFlowPanel createTimePanelLayoutWrapper() {
        ResizableFlowPanel timeLineInnerBgPanel = new ResizableFlowPanel();
        timeLineInnerBgPanel.addStyleName("timeLineInnerBgPanel");
        timeLineInnerBgPanel.add(getRaceTimePanel());
        
        ResizableFlowPanel timeLineInnerPanel = new ResizableFlowPanel();
        timeLineInnerPanel.add(timeLineInnerBgPanel);
        timeLineInnerPanel.addStyleName("timeLineInnerPanel");
        
        ResizableFlowPanel timelinePanel = new ResizableFlowPanel();
        timelinePanel.add(timeLineInnerPanel);
        timelinePanel.addStyleName("timeLinePanel");
        
        return timelinePanel;
    }

    @Override
    public SettingsDialogComponent<RaceBoardPerspectiveOwnSettings> getPerspectiveOwnSettingsDialogComponent() {
        return new RaceBoardPerspectiveSettingsDialogComponent(getPerspectiveSettings(), stringMessages);
    }

    @Override
    public boolean hasPerspectiveOwnSettings() {
        return true;
    }

    @Override
    public void onResize() {
        dockPanel.onResize();        
    }

    @Override
    public String getId() {
        return RaceBoardPerspectiveLifecycle.ID;
    }
}

