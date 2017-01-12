package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.LeaderboardType;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.common.authentication.FixedSailingAuthentication;
import com.sap.sailing.gwt.common.authentication.SAPSailingHeaderWithAuthentication;
import com.sap.sailing.gwt.ui.client.AbstractSailingEntryPoint;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings.RaceColumnSelectionStrategies;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.player.Timer.PlayStates;
import com.sap.sse.gwt.client.shared.perspective.OnSettingsLoadedCallback;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycleWithAllSettings;
import com.sap.sse.gwt.settings.SettingsToUrlSerializer;
import com.sap.sse.gwt.shared.GwtHttpRequestUtils;
import com.sap.sse.security.ui.settings.UserSettingsStorageManager;


public class LeaderboardEntryPoint extends AbstractSailingEntryPoint {
    public static final long DEFAULT_REFRESH_INTERVAL_MILLIS = 3000l;

    private static final Logger logger = Logger.getLogger(LeaderboardEntryPoint.class.getName());
    
    private final SettingsToUrlSerializer settingsToUrlSerializer = new SettingsToUrlSerializer();

    private String leaderboardName;
    private String leaderboardGroupName;
    private LeaderboardType leaderboardType;
    private EventDTO event;
    
    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        final LeaderboardContextSettings leaderboardContextSettings = settingsToUrlSerializer.deserializeFromCurrentLocation(new LeaderboardContextSettings());
        
        final UUID eventId = leaderboardContextSettings.getEventId();

        leaderboardName = leaderboardContextSettings.getLeaderboardName();
        leaderboardGroupName = leaderboardContextSettings.getLeaderboardGroupName();

        if (leaderboardName != null) {
            final Runnable checkLeaderboardNameAndCreateUI = new Runnable() {
                @Override
                public void run() {
                    sailingService.checkLeaderboardName(leaderboardName,
                            new MarkedAsyncCallback<Util.Pair<String, LeaderboardType>>(
                                    new AsyncCallback<Util.Pair<String, LeaderboardType>>() {
                                        @Override
                                        public void onSuccess(Util.Pair<String, LeaderboardType> leaderboardNameAndType) {
                                            if (leaderboardNameAndType != null
                                                    && leaderboardName.equals(leaderboardNameAndType.getA())) {
                                                Window.setTitle(leaderboardName);
                                                leaderboardType = leaderboardNameAndType.getB();
                                                createUI(leaderboardContextSettings, event);
                                            } else {
                                                RootPanel.get().add(new Label(getStringMessages().noSuchLeaderboard()));
                                            }
                                        }

                                        @Override
                                        public void onFailure(Throwable t) {
                                            reportError("Error trying to obtain list of leaderboard names: "
                                                    + t.getMessage());
                                        }
                                    }));
                }
            };
            if (eventId == null) {
                checkLeaderboardNameAndCreateUI.run(); // use null-initialized event field
            } else {
                sailingService.getEventById(eventId, /* withStatisticalData */false, new MarkedAsyncCallback<EventDTO>(
                        new AsyncCallback<EventDTO>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                reportError("Error trying to obtain event "+eventId+": " + caught.getMessage());
                            }

                            @Override
                            public void onSuccess(EventDTO result) {
                                event = result;
                                checkLeaderboardNameAndCreateUI.run();
                            }
                        }));
            }
        } else {
            RootPanel.get().add(new Label(getStringMessages().noSuchLeaderboard()));
        }
        final String zoomTo = leaderboardContextSettings.getZoomTo();
        if (zoomTo != null) {
            RootPanel.getBodyElement().setAttribute(
                    "style",
                    "zoom: " + zoomTo + ";-moz-transform: scale(" + zoomTo
                            + ");-moz-transform-origin: 0 0;-o-transform: scale(" + zoomTo
                            + ");-o-transform-origin: 0 0;-webkit-transform: scale(" + zoomTo
                            + ");-webkit-transform-origin: 0 0;");
        }
    }
    
    private void createUI(LeaderboardContextSettings leaderboardContextSettings, EventDTO event) {
        DockLayoutPanel mainPanel = new DockLayoutPanel(Unit.PX);
        RootLayoutPanel.get().add(mainPanel);
        if (!leaderboardContextSettings.getEmbedded()) {
            // Hack to shorten the leaderboardName in case of overall leaderboards
            String leaderboardDisplayName = leaderboardContextSettings.getDisplayName();
            if (leaderboardDisplayName == null || leaderboardDisplayName.isEmpty()) {
                leaderboardDisplayName = leaderboardName;
            }
            SAPSailingHeaderWithAuthentication header  = new SAPSailingHeaderWithAuthentication(leaderboardDisplayName);
            new FixedSailingAuthentication(getUserService(), header.getAuthenticationMenuView());
            mainPanel.addNorth(header, 75);
        }
        ScrollPanel contentScrollPanel = new ScrollPanel();
        long delayBetweenAutoAdvancesInMilliseconds = DEFAULT_REFRESH_INTERVAL_MILLIS;
        Timer timer = new Timer(PlayModes.Live, PlayStates.Paused, delayBetweenAutoAdvancesInMilliseconds);
        final LeaderboardSettings leaderboardSettings = createLeaderboardSettingsFromURLParameters(Window.Location.getParameterMap());
        if (leaderboardSettings.getDelayBetweenAutoAdvancesInMilliseconds() != null) {
            timer.setPlayMode(PlayModes.Live); // the leaderboard, viewed via the entry point, goes "live" and "playing" if an auto-refresh
        } // interval has been specified
        String chartDetailParam = GwtHttpRequestUtils.getStringParameter(LeaderboardUrlSettings.PARAM_CHART_DETAIL, null);
        final DetailType chartDetailType;
        if (chartDetailParam != null && (DetailType.REGATTA_RANK.name().equals(chartDetailParam) || DetailType.OVERALL_RANK.name().equals(chartDetailParam) || 
                DetailType.REGATTA_NET_POINTS_SUM.name().equals(chartDetailParam))) {
            chartDetailType = DetailType.valueOf(chartDetailParam);
        } else {
            chartDetailType = leaderboardType.isMetaLeaderboard() ?  DetailType.OVERALL_RANK : DetailType.REGATTA_RANK;
        }

        final RegattaAndRaceIdentifier preselectedRace = getPreselectedRace(Window.Location.getParameterMap());
        // make a single live request as the default but don't continue to play by default
        
        if (leaderboardType.isMetaLeaderboard()) {
            final MetaLeaderboardPerspectiveLifecycle lifecycle = new MetaLeaderboardPerspectiveLifecycle(getStringMessages());
            final MetaLeaderboardComponentContext componentContext = new MetaLeaderboardComponentContext(lifecycle, new UserSettingsStorageManager<>(getUserService(), "TODO1", "TODO2"));
            componentContext.initInitialSettings(new OnSettingsLoadedCallback<PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings>>() {
                @Override
                public void onSuccess(PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings> defaultSettings) {
                    PerspectiveLifecycleWithAllSettings<MetaLeaderboardPerspectiveLifecycle, LeaderboardPerspectiveOwnSettings> lifecycleWithAllSettings = new PerspectiveLifecycleWithAllSettings<>(lifecycle, defaultSettings);
                    MetaLeaderboardViewer leaderboardViewer = new MetaLeaderboardViewer(componentContext, lifecycleWithAllSettings, sailingService, new AsyncActionsExecutor(),
                    timer, null, preselectedRace, leaderboardGroupName, leaderboardName, LeaderboardEntryPoint.this, getStringMessages(),
                    userAgent, chartDetailType);
                    contentScrollPanel.setWidget(leaderboardViewer);
                }
                
                @Override
                public void onError(Throwable caught,
                        PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings> fallbackDefaultSettings) {
                    // TODO
                    onSuccess(fallbackDefaultSettings);
                }
            });
        } else {
            final LeaderboardPerspectiveLifecycle lifecycle = new LeaderboardPerspectiveLifecycle(getStringMessages());
            final LeaderboardComponentContext componentContext = new LeaderboardComponentContext(lifecycle, new UserSettingsStorageManager<>(getUserService(), "TODO3", "TODO4"));
            componentContext.initInitialSettings(new OnSettingsLoadedCallback<PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings>>() {
                @Override
                public void onSuccess(PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings> defaultSettings) {
                    PerspectiveLifecycleWithAllSettings<LeaderboardPerspectiveLifecycle, LeaderboardPerspectiveOwnSettings> lifecycleWithAllSettings = new PerspectiveLifecycleWithAllSettings<>(lifecycle, defaultSettings);
                    LeaderboardViewer leaderboardViewer = new LeaderboardViewer(
                            componentContext, lifecycleWithAllSettings, sailingService, new AsyncActionsExecutor(),
                            timer, preselectedRace, leaderboardGroupName, leaderboardName, LeaderboardEntryPoint.this, getStringMessages(),
                            userAgent, chartDetailType);
                    contentScrollPanel.setWidget(leaderboardViewer);
                }
                
                @Override
                public void onError(Throwable caught,
                        PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings> fallbackDefaultSettings) {
                    // TODO
                    onSuccess(fallbackDefaultSettings);
                }
            });
        }
        mainPanel.add(contentScrollPanel);
    }
   
    private RegattaAndRaceIdentifier getPreselectedRace(Map<String, List<String>> parameterMap) {
        RegattaAndRaceIdentifier result;
        if (parameterMap.containsKey(LeaderboardUrlSettings.PARAM_RACE_NAME) && parameterMap.get(LeaderboardUrlSettings.PARAM_RACE_NAME).size() == 1 &&
                parameterMap.containsKey(LeaderboardUrlSettings.PARAM_REGATTA_NAME) && parameterMap.get(LeaderboardUrlSettings.PARAM_REGATTA_NAME).size() == 1) {
            result = new RegattaNameAndRaceName(parameterMap.get(LeaderboardUrlSettings.PARAM_REGATTA_NAME).get(0), parameterMap.get(LeaderboardUrlSettings.PARAM_RACE_NAME).get(0));
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Constructs {@link LeaderboardSettings} from the URL parameters found
     */
    private LeaderboardSettings createLeaderboardSettingsFromURLParameters(Map<String, List<String>> parameterMap) {
        LeaderboardSettings result;
        Long refreshIntervalMillis = parameterMap.containsKey(LeaderboardUrlSettings.PARAM_REFRESH_INTERVAL_MILLIS) ? Long
                .valueOf(parameterMap.get(LeaderboardUrlSettings.PARAM_REFRESH_INTERVAL_MILLIS).get(0)) : null;
        RaceColumnSelectionStrategies raceColumnSelectionStrategy;
        final Integer numberOfLastRacesToShow;
        if (parameterMap.containsKey(LeaderboardUrlSettings.PARAM_NAME_LAST_N)) {
            raceColumnSelectionStrategy = RaceColumnSelectionStrategies.LAST_N;
            numberOfLastRacesToShow = Integer.valueOf(parameterMap.get(LeaderboardUrlSettings.PARAM_NAME_LAST_N).get(0));
        } else if (isSmallWidth()) {
            raceColumnSelectionStrategy = RaceColumnSelectionStrategies.LAST_N;
            int width = Window.getClientWidth();
            numberOfLastRacesToShow = (width-275)/40;
        } else {
            raceColumnSelectionStrategy = RaceColumnSelectionStrategies.EXPLICIT;
            numberOfLastRacesToShow = null;
        }
        if (parameterMap.containsKey(LeaderboardUrlSettings.PARAM_RACE_NAME) || parameterMap.containsKey(LeaderboardUrlSettings.PARAM_RACE_DETAIL) ||
                parameterMap.containsKey(LeaderboardUrlSettings.PARAM_LEG_DETAIL) || parameterMap.containsKey(LeaderboardUrlSettings.PARAM_MANEUVER_DETAIL) ||
                parameterMap.containsKey(LeaderboardUrlSettings.PARAM_OVERALL_DETAIL) || parameterMap.containsKey(LeaderboardUrlSettings.PARAM_SHOW_ADDED_SCORES) ||
                parameterMap.containsKey(LeaderboardUrlSettings.PARAM_SHOW_OVERALL_COLUMN_WITH_NUMBER_OF_RACES_COMPLETED) 
                || parameterMap.containsKey(LeaderboardUrlSettings.PARAM_SHOW_COMPETITOR_NAME_COLUMNS)) {
            List<DetailType> maneuverDetails = getDetailTypeListFromParamValue(parameterMap.get(LeaderboardUrlSettings.PARAM_MANEUVER_DETAIL));
            List<DetailType> raceDetails = getDetailTypeListFromParamValue(parameterMap.get(LeaderboardUrlSettings.PARAM_RACE_DETAIL));
            List<DetailType> overallDetails = getDetailTypeListFromParamValue(parameterMap.get(LeaderboardUrlSettings.PARAM_OVERALL_DETAIL));
            if (overallDetails.isEmpty()) {
                overallDetails = Collections.singletonList(DetailType.REGATTA_RANK);
            }
            List<DetailType> legDetails = getDetailTypeListFromParamValue(parameterMap.get(LeaderboardUrlSettings.PARAM_LEG_DETAIL));
            List<String> namesOfRacesToShow = getStringListFromParamValue(parameterMap.get(LeaderboardUrlSettings.PARAM_RACE_NAME));
            boolean showAddedScores = parameterMap.containsKey(LeaderboardUrlSettings.PARAM_SHOW_ADDED_SCORES) ? 
                    Boolean.valueOf(parameterMap.get(LeaderboardUrlSettings.PARAM_SHOW_ADDED_SCORES).get(0)) : false;
            boolean showOverallColumnWithNumberOfRacesSailedPerCompetitor = parameterMap.containsKey(LeaderboardUrlSettings.PARAM_SHOW_OVERALL_COLUMN_WITH_NUMBER_OF_RACES_COMPLETED) ?
                    Boolean.valueOf(parameterMap.get(LeaderboardUrlSettings.PARAM_SHOW_OVERALL_COLUMN_WITH_NUMBER_OF_RACES_COMPLETED).get(0)) : false;
            boolean autoExpandPreSelectedRace = parameterMap.containsKey(LeaderboardUrlSettings.PARAM_AUTO_EXPAND_PRESELECTED_RACE) ?
                    Boolean.valueOf(parameterMap.get(LeaderboardUrlSettings.PARAM_AUTO_EXPAND_PRESELECTED_RACE).get(0)) :
                        (namesOfRacesToShow != null && namesOfRacesToShow.size() == 1);
           boolean showCompetitorSailIdColumn = true;
           boolean showCompetitorFullNameColumn = true;
           if (parameterMap.containsKey(LeaderboardUrlSettings.PARAM_SHOW_COMPETITOR_NAME_COLUMNS)) {
                String value = parameterMap.get(LeaderboardUrlSettings.PARAM_SHOW_COMPETITOR_NAME_COLUMNS).get(0);
                if (value.equals(LeaderboardUrlSettings.COMPETITOR_NAME_COLUMN_FULL_NAME)) {
                    showCompetitorSailIdColumn = false;
                } else if (value.equals(LeaderboardUrlSettings.COMPETITOR_NAME_COLUMN_SAIL_ID)) {
                    showCompetitorFullNameColumn = false;
                } else if (value.trim().equals("")) {
                    showCompetitorFullNameColumn = false;
                    showCompetitorSailIdColumn = false;
                }
            }
            result = new LeaderboardSettings(maneuverDetails, legDetails, raceDetails, overallDetails,
                    /* namesOfRaceColumnsToShow */ null,
                    namesOfRacesToShow, numberOfLastRacesToShow,
                    autoExpandPreSelectedRace, refreshIntervalMillis, /* sort by column */ (namesOfRacesToShow != null && !namesOfRacesToShow.isEmpty()) ?
                                    namesOfRacesToShow.get(0) : null,
                            /* ascending */ true, /* updateUponPlayStateChange */ raceDetails.isEmpty() && legDetails.isEmpty(),
                                    raceColumnSelectionStrategy, showAddedScores, showOverallColumnWithNumberOfRacesSailedPerCompetitor,
                                    showCompetitorSailIdColumn, showCompetitorFullNameColumn);

        } else {
            return new LeaderboardSettings(refreshIntervalMillis, numberOfLastRacesToShow, raceColumnSelectionStrategy);
        }
        return result;
    }

    private List<DetailType> getDetailTypeListFromParamValue(List<String> list) {
        List<DetailType> result = new ArrayList<DetailType>();
        if (list != null) {
            for (String entry : list) {
                try {
                    result.add(DetailType.valueOf(entry));
                } catch (IllegalArgumentException e) {
                    logger.info("Can't find detail type "+entry+". Ignoring.");
                }
            }
        }
        return result;
    }

    private List<String> getStringListFromParamValue(List<String> list) {
        List<String> result = new ArrayList<String>();
        if (list != null) {
            result.addAll(list);
        }
        return result;
    }
    
    /**
     * Assembles a dialog that other parts of the application can use to let the user parameterize a leaderboard and
     * obtain the according URL for it. This keeps the "secrets" of which URL parameters have which meaning encapsulated
     * within this class.<p>
     * 
     * The implementation by and large uses the {@link LeaderboardSettingsDialogComponent}'s widget and adds to it a checkbox
     * for driving the {@link #LeaderboardUrlSettings.PARAM_EMBEDDED} field.
     * 
     * @see LeaderboardEntryPoint#getUrl(String, LeaderboardSettings, boolean)
     */
    public static DataEntryDialog<LeaderboardUrlSettings> getUrlConfigurationDialog(final AbstractLeaderboardDTO leaderboard,
            final StringMessages stringMessages) {
        return new LeaderboardUrlConfigurationDialog(stringMessages, leaderboard);
    }

}
