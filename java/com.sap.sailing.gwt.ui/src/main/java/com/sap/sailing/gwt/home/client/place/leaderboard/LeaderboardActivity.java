package com.sap.sailing.gwt.home.client.place.leaderboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.LeaderboardType;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.client.shared.placeholder.Placeholder;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings.RaceColumnSelectionStrategies;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettingsFactory;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardUrlSettings;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.mvp.ErrorView;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.player.Timer.PlayStates;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;
import com.sap.sse.gwt.shared.GwtHttpRequestUtils;

public class LeaderboardActivity extends AbstractActivity implements ErrorReporter {
    private final LeaderboardClientFactory clientFactory;
    private final LeaderboardPlace leaderboardPlace;
    private final Timer timerForClientServerOffset;

    private final UserAgentDetails userAgent = new UserAgentDetails(Window.Navigator.getUserAgent());

    public LeaderboardActivity(LeaderboardPlace place, LeaderboardClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        this.leaderboardPlace = place;
        
        timerForClientServerOffset = new Timer(PlayModes.Replay);
    }

    @Override
    public void start(final AcceptsOneWidget panel, EventBus eventBus) {
        panel.setWidget(new Placeholder());

        UUID eventUUID = UUID.fromString(leaderboardPlace.getEventUuidAsString());
        final String leaderboardName = leaderboardPlace.getLeaderboardIdAsNameString();
        final boolean showRaceDetails = leaderboardPlace.getShowRaceDetails();

        clientFactory.getSailingService().getEventById(eventUUID, true, new AsyncCallback<EventDTO>() {
            @Override
            public void onSuccess(final EventDTO event) {
                if (leaderboardName != null) {
                    clientFactory.getSailingService().checkLeaderboardName(leaderboardName, new MarkedAsyncCallback<Util.Pair<String, LeaderboardType>>(
                            new AsyncCallback<Util.Pair<String, LeaderboardType>>() {
                                @Override
                                public void onSuccess(Util.Pair<String, LeaderboardType> leaderboardNameAndType) {
                                    if (leaderboardNameAndType != null && leaderboardName.equals(leaderboardNameAndType.getA())) {
                                        createAnalyticsViewer(panel, event, leaderboardNameAndType.getA(), leaderboardNameAndType.getB(), showRaceDetails);
                                    } else {
                                        createErrorView(TextMessages.INSTANCE.errorMessageNoSuchLeaderboard(), null, panel);
                                    }
                                }
                
                                @Override
                                public void onFailure(Throwable caught) {
                                    createErrorView("Error while checking the leaderboard name with service checkLeaderboardName()", caught, panel);
                                }
                            }));
                } else {
                    createErrorView(TextMessages.INSTANCE.errorMessageNoSuchLeaderboard(), null, panel);
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                createErrorView("Error while loading the event with service getEventById()", caught, panel);
            }
        }); 
    }
    
    private void createAnalyticsViewer(AcceptsOneWidget panel, EventDTO event, String leaderboardName, LeaderboardType leaderboardType, boolean showRaceDetails) {
        String leaderboardDisplayName = Window.Location.getParameter("displayName");
        if (leaderboardDisplayName == null || leaderboardDisplayName.isEmpty()) {
            leaderboardDisplayName = leaderboardName;
        }
            
        long delayBetweenAutoAdvancesInMilliseconds = 3000l;
        final RaceIdentifier preselectedRace = getPreselectedRace(Window.Location.getParameterMap());
        // make a single live request as the default but don't continue to play by default
        Timer timer = new Timer(PlayModes.Live, PlayStates.Paused, delayBetweenAutoAdvancesInMilliseconds);
        final LeaderboardSettings leaderboardSettings = createLeaderboardSettingsFromURLParameters(Window.Location.getParameterMap());
        if (leaderboardSettings.getDelayBetweenAutoAdvancesInMilliseconds() != null) {
            timer.setPlayMode(PlayModes.Live); // the leaderboard, viewed via the entry point, goes "live" and "playing" if an auto-refresh
        } // interval has been specified
        boolean autoExpandLastRaceColumn = GwtHttpRequestUtils.getBooleanParameter(LeaderboardUrlSettings.PARAM_AUTO_EXPAND_LAST_RACE_COLUMN, false);
        boolean showOverallLeaderboard = GwtHttpRequestUtils.getBooleanParameter(LeaderboardUrlSettings.PARAM_SHOW_OVERALL_LEADERBOARD, false);
        boolean showSeriesLeaderboards = GwtHttpRequestUtils.getBooleanParameter(LeaderboardUrlSettings.PARAM_SHOW_SERIES_LEADERBOARDS, false);
        
        AnalyticsView analyticsView = clientFactory.createLeaderboardView(event, leaderboardName, timerForClientServerOffset);
        Window.setTitle(leaderboardPlace.getTitle(event.getName(), leaderboardDisplayName));
        
        if (leaderboardType.isMetaLeaderboard()) {
            analyticsView.createSeriesAnalyticsViewer(clientFactory.getSailingService(), new AsyncActionsExecutor(),
                    timer, leaderboardSettings, null, preselectedRace, "leaderboardGroupName", leaderboardName, this,
                    userAgent, showRaceDetails, autoExpandLastRaceColumn, showSeriesLeaderboards);
        } else {
            analyticsView.createRegattaAnalyticsViewer(clientFactory.getSailingService(), new AsyncActionsExecutor(),
                    timer, leaderboardSettings, preselectedRace, "leaderboardGroupName", leaderboardName, this,
                    userAgent, showRaceDetails, autoExpandLastRaceColumn, showOverallLeaderboard);
        }

        panel.setWidget(analyticsView.asWidget());
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
                parameterMap.containsKey(LeaderboardUrlSettings.PARAM_SHOW_OVERALL_COLUMN_WITH_NUMBER_OF_RACES_COMPLETED)) {
            List<DetailType> maneuverDetails = getDetailTypeListFromParamValue(parameterMap.get(LeaderboardUrlSettings.PARAM_MANEUVER_DETAIL));
            List<DetailType> raceDetails = getDetailTypeListFromParamValue(parameterMap.get(LeaderboardUrlSettings.PARAM_RACE_DETAIL));
            List<DetailType> overallDetails = getDetailTypeListFromParamValue(parameterMap.get(LeaderboardUrlSettings.PARAM_OVERALL_DETAIL));
            List<DetailType> legDetails = getDetailTypeListFromParamValue(parameterMap.get(LeaderboardUrlSettings.PARAM_LEG_DETAIL));
            List<String> namesOfRacesToShow = getStringListFromParamValue(parameterMap.get(LeaderboardUrlSettings.PARAM_RACE_NAME));
            boolean showAddedScores = parameterMap.containsKey(LeaderboardUrlSettings.PARAM_SHOW_ADDED_SCORES) ? 
                    Boolean.valueOf(parameterMap.get(LeaderboardUrlSettings.PARAM_SHOW_ADDED_SCORES).get(0)) : false;
            boolean showOverallColumnWithNumberOfRacesSailedPerCompetitor = parameterMap.containsKey(LeaderboardUrlSettings.PARAM_SHOW_OVERALL_COLUMN_WITH_NUMBER_OF_RACES_COMPLETED) ?
                    Boolean.valueOf(parameterMap.get(LeaderboardUrlSettings.PARAM_SHOW_OVERALL_COLUMN_WITH_NUMBER_OF_RACES_COMPLETED).get(0)) : false;
            boolean autoExpandPreSelectedRace = parameterMap.containsKey(LeaderboardUrlSettings.PARAM_AUTO_EXPAND_PRESELECTED_RACE) ?
                    Boolean.valueOf(parameterMap.get(LeaderboardUrlSettings.PARAM_AUTO_EXPAND_PRESELECTED_RACE).get(0)) :
                        (namesOfRacesToShow != null && namesOfRacesToShow.size() == 1);
            result = new LeaderboardSettings(maneuverDetails, legDetails, raceDetails, overallDetails,
                    /* namesOfRaceColumnsToShow */ null,
                    namesOfRacesToShow, numberOfLastRacesToShow,
                    autoExpandPreSelectedRace, refreshIntervalMillis, /* sort by column */ (namesOfRacesToShow != null && !namesOfRacesToShow.isEmpty()) ?
                                    namesOfRacesToShow.get(0) : null,
                            /* ascending */ true, /* updateUponPlayStateChange */ raceDetails.isEmpty() && legDetails.isEmpty(),
                                    raceColumnSelectionStrategy, showAddedScores, showOverallColumnWithNumberOfRacesSailedPerCompetitor);

        } else {
            final List<DetailType> overallDetails = Collections.singletonList(DetailType.REGATTA_RANK);
            result = LeaderboardSettingsFactory.getInstance().createNewDefaultSettings(null, null,
                    /* overallDetails */ overallDetails, null,
                    /* autoExpandFirstRace */false, refreshIntervalMillis, numberOfLastRacesToShow,
                    raceColumnSelectionStrategy);
        }
        return result;
    }

    private RaceIdentifier getPreselectedRace(Map<String, List<String>> parameterMap) {
        RaceIdentifier result;
        if (parameterMap.containsKey(LeaderboardUrlSettings.PARAM_RACE_NAME) && parameterMap.get(LeaderboardUrlSettings.PARAM_RACE_NAME).size() == 1 &&
                parameterMap.containsKey(LeaderboardUrlSettings.PARAM_REGATTA_NAME) && parameterMap.get(LeaderboardUrlSettings.PARAM_REGATTA_NAME).size() == 1) {
            result = new RegattaNameAndRaceName(parameterMap.get(LeaderboardUrlSettings.PARAM_REGATTA_NAME).get(0), parameterMap.get(LeaderboardUrlSettings.PARAM_RACE_NAME).get(0));
        } else {
            result = null;
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
    
    private void createErrorView(String errorMessage, Throwable errorReason, AcceptsOneWidget panel) {
        ErrorView view = clientFactory.createErrorView(errorMessage, errorReason);
        panel.setWidget(view.asWidget());
    }

    public boolean isSmallWidth() {
        int width = Window.getClientWidth();
        return width <= 720;
    }
    
    @Override
    public void reportError(String message) {
        Window.alert(message);
    }

    @Override
    public void reportError(String message, boolean silentMode) {
        if (silentMode) {
            Window.setStatus(message);
        } else {
            reportError(message);
        }
    }

    @Override
    public void reportPersistentInformation(String message) {
        Window.setStatus(message);
    }

}
