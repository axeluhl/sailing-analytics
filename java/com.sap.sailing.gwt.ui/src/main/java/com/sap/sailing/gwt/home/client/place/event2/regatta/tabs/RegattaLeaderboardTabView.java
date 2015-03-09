package com.sap.sailing.gwt.home.client.place.event2.regatta.tabs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.place.event.oldleaderboard.OldLeaderboard;
import com.sap.sailing.gwt.home.client.place.event.regattaanalytics.RegattaAnalyticsDataManager;
import com.sap.sailing.gwt.home.client.place.event2.regatta.EventRegattaView;
import com.sap.sailing.gwt.home.client.place.event2.regatta.EventRegattaView.Presenter;
import com.sap.sailing.gwt.home.client.place.event2.regatta.RegattaTabView;
import com.sap.sailing.gwt.home.client.shared.placeholder.Placeholder;
import com.sap.sailing.gwt.ui.client.LeaderboardUpdateListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings.RaceColumnSelectionStrategies;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettingsFactory;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardUrlSettings;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.player.Timer.PlayStates;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;
import com.sap.sse.gwt.shared.GwtHttpRequestUtils;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class RegattaLeaderboardTabView extends Composite implements RegattaTabView<RegattaLeaderboardPlace>,
        LeaderboardUpdateListener {

    private LeaderboardDTO leaderboardDTO;
    private Presenter currentPresenter;

    @UiField
    protected OldLeaderboard leaderboard;
    private RegattaAnalyticsDataManager regattaAnalyticsManager;
    private final UserAgentDetails userAgent = new UserAgentDetails(Window.Navigator.getUserAgent());
    private final AsyncActionsExecutor asyncActionsExecutor = new AsyncActionsExecutor();
    public RegattaLeaderboardTabView() {

    }

    @Override
    public Class<RegattaLeaderboardPlace> getPlaceClassForActivation() {
        return RegattaLeaderboardPlace.class;
    }

    @Override
    public void setPresenter(EventRegattaView.Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
    }
    
    @Override
    public TabView.State getState() {
        return TabView.State.VISIBLE;
    }

    @Override
    public void start(final RegattaLeaderboardPlace myPlace, final AcceptsOneWidget contentArea) {

        contentArea.setWidget(new Placeholder());

        String regattaId = myPlace.getRegattaId();

        if (regattaId != null && !regattaId.isEmpty()) {

            String leaderboardName = regattaId;

            long delayBetweenAutoAdvancesInMilliseconds = 3000l;
            Timer autoRefreshTimer = new Timer(PlayModes.Live, PlayStates.Paused,
                    delayBetweenAutoAdvancesInMilliseconds);

            ErrorReporter errorReporter = currentPresenter.getErrorReporter();

            SailingServiceAsync sailingService = currentPresenter.getSailingService();

            boolean autoExpandLastRaceColumn = GwtHttpRequestUtils.getBooleanParameter(
                    LeaderboardUrlSettings.PARAM_AUTO_EXPAND_LAST_RACE_COLUMN, false);

            final LeaderboardSettings leaderboardSettings = createLeaderboardSettingsFromURLParameters(Window.Location
                    .getParameterMap());

            final RaceIdentifier preselectedRace = getPreselectedRace(Window.Location.getParameterMap());

            regattaAnalyticsManager = new RegattaAnalyticsDataManager( //
                    sailingService, //
                    asyncActionsExecutor, //
                    autoRefreshTimer, //
                    errorReporter, //
                    userAgent);

            regattaAnalyticsManager.createLeaderboardPanel( //
                    leaderboardSettings, //
                    preselectedRace, //
                    "leaderboardGroupName", // TODO: keep using magic string? ask frank!
                    leaderboardName, //
                    true, // this information came from place, now hard coded. check with frank
                    autoExpandLastRaceColumn);

            List<DetailType> availableDetailsTypes = new ArrayList<DetailType>();
            DetailType initialDetailType = DetailType.REGATTA_RANK;
            availableDetailsTypes.add(DetailType.REGATTA_RANK);
            availableDetailsTypes.add(DetailType.REGATTA_TOTAL_POINTS_SUM);
            regattaAnalyticsManager.createMultiCompetitorChart(leaderboardName, initialDetailType);

            initWidget(ourUiBinder.createAndBindUi(this));

            leaderboard.setLeaderboard(regattaAnalyticsManager.getLeaderboardPanel(), autoRefreshTimer);

            regattaAnalyticsManager.getLeaderboardPanel().addLeaderboardUpdateListener(this);

            regattaAnalyticsManager.hideCompetitorChart();


            contentArea.setWidget(this);
        } else {
            contentArea.setWidget(new Label("No leaderboard specified, cannot proceed to leaderboardpage"));
            new com.google.gwt.user.client.Timer() {
                @Override
                public void run() {
                    currentPresenter.getHomeNavigation().goToPlace();
                }
            }.schedule(3000);

        }
    }

    @Override
    public void currentRaceSelected(RaceIdentifier raceIdentifier, RaceColumnDTO raceColumn) {

    }

    @Override
    public void stop() {

    }

    interface MyBinder extends UiBinder<HTMLPanel, RegattaLeaderboardTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);

    @Override
    public RegattaLeaderboardPlace placeToFire() {
        return new RegattaLeaderboardPlace(currentPresenter.getCtx());
    }

    private LeaderboardSettings createLeaderboardSettingsFromURLParameters(Map<String, List<String>> parameterMap) {
        LeaderboardSettings result;
        Long refreshIntervalMillis = parameterMap.containsKey(LeaderboardUrlSettings.PARAM_REFRESH_INTERVAL_MILLIS) ? Long
                .valueOf(parameterMap.get(LeaderboardUrlSettings.PARAM_REFRESH_INTERVAL_MILLIS).get(0)) : null;
        RaceColumnSelectionStrategies raceColumnSelectionStrategy;
        final Integer numberOfLastRacesToShow;
        if (parameterMap.containsKey(LeaderboardUrlSettings.PARAM_NAME_LAST_N)) {
            raceColumnSelectionStrategy = RaceColumnSelectionStrategies.LAST_N;
            numberOfLastRacesToShow = Integer
                    .valueOf(parameterMap.get(LeaderboardUrlSettings.PARAM_NAME_LAST_N).get(0));
        } else if (isSmallWidth()) {
            raceColumnSelectionStrategy = RaceColumnSelectionStrategies.LAST_N;
            int width = Window.getClientWidth();
            numberOfLastRacesToShow = (width - 275) / 40;
        } else {
            raceColumnSelectionStrategy = RaceColumnSelectionStrategies.EXPLICIT;
            numberOfLastRacesToShow = null;
        }
        if (parameterMap.containsKey(LeaderboardUrlSettings.PARAM_RACE_NAME)
                || parameterMap.containsKey(LeaderboardUrlSettings.PARAM_RACE_DETAIL)
                || parameterMap.containsKey(LeaderboardUrlSettings.PARAM_LEG_DETAIL)
                || parameterMap.containsKey(LeaderboardUrlSettings.PARAM_MANEUVER_DETAIL)
                || parameterMap.containsKey(LeaderboardUrlSettings.PARAM_OVERALL_DETAIL)
                || parameterMap.containsKey(LeaderboardUrlSettings.PARAM_SHOW_ADDED_SCORES)
                || parameterMap
                        .containsKey(LeaderboardUrlSettings.PARAM_SHOW_OVERALL_COLUMN_WITH_NUMBER_OF_RACES_COMPLETED)) {
            List<DetailType> maneuverDetails = getDetailTypeListFromParamValue(parameterMap
                    .get(LeaderboardUrlSettings.PARAM_MANEUVER_DETAIL));
            List<DetailType> raceDetails = getDetailTypeListFromParamValue(parameterMap
                    .get(LeaderboardUrlSettings.PARAM_RACE_DETAIL));
            List<DetailType> overallDetails = getDetailTypeListFromParamValue(parameterMap
                    .get(LeaderboardUrlSettings.PARAM_OVERALL_DETAIL));
            if (overallDetails.isEmpty()) {
                overallDetails = Collections.singletonList(DetailType.REGATTA_RANK);
            }
            List<DetailType> legDetails = getDetailTypeListFromParamValue(parameterMap
                    .get(LeaderboardUrlSettings.PARAM_LEG_DETAIL));
            List<String> namesOfRacesToShow = getStringListFromParamValue(parameterMap
                    .get(LeaderboardUrlSettings.PARAM_RACE_NAME));
            boolean showAddedScores = parameterMap.containsKey(LeaderboardUrlSettings.PARAM_SHOW_ADDED_SCORES) ? Boolean
                    .valueOf(parameterMap.get(LeaderboardUrlSettings.PARAM_SHOW_ADDED_SCORES).get(0)) : false;
            boolean showOverallColumnWithNumberOfRacesSailedPerCompetitor = parameterMap
                    .containsKey(LeaderboardUrlSettings.PARAM_SHOW_OVERALL_COLUMN_WITH_NUMBER_OF_RACES_COMPLETED) ? Boolean
                    .valueOf(parameterMap.get(
                            LeaderboardUrlSettings.PARAM_SHOW_OVERALL_COLUMN_WITH_NUMBER_OF_RACES_COMPLETED).get(0))
                    : false;
            boolean autoExpandPreSelectedRace = parameterMap
                    .containsKey(LeaderboardUrlSettings.PARAM_AUTO_EXPAND_PRESELECTED_RACE) ? Boolean
                    .valueOf(parameterMap.get(LeaderboardUrlSettings.PARAM_AUTO_EXPAND_PRESELECTED_RACE).get(0))
                    : (namesOfRacesToShow != null && namesOfRacesToShow.size() == 1);
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
            /* namesOfRaceColumnsToShow */null, namesOfRacesToShow, numberOfLastRacesToShow, autoExpandPreSelectedRace,
                    refreshIntervalMillis, /* sort by column */
                    (namesOfRacesToShow != null && !namesOfRacesToShow.isEmpty()) ? namesOfRacesToShow.get(0) : null,
                    /* ascending */true, /* updateUponPlayStateChange */raceDetails.isEmpty() && legDetails.isEmpty(),
                    raceColumnSelectionStrategy, showAddedScores,
                    showOverallColumnWithNumberOfRacesSailedPerCompetitor, showCompetitorSailIdColumn,
                    showCompetitorFullNameColumn);

        } else {
            final List<DetailType> overallDetails = Collections.singletonList(DetailType.REGATTA_RANK);
            result = LeaderboardSettingsFactory.getInstance()
                    .createNewDefaultSettings(null, null,
                    /* overallDetails */overallDetails, null,
                    /* autoExpandFirstRace */false, refreshIntervalMillis, numberOfLastRacesToShow,
                            raceColumnSelectionStrategy);
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

    public boolean isSmallWidth() {
        int width = Window.getClientWidth();
        return width <= 720;
    }

    private RaceIdentifier getPreselectedRace(Map<String, List<String>> parameterMap) {
        RaceIdentifier result;
        if (parameterMap.containsKey(LeaderboardUrlSettings.PARAM_RACE_NAME)
                && parameterMap.get(LeaderboardUrlSettings.PARAM_RACE_NAME).size() == 1
                && parameterMap.containsKey(LeaderboardUrlSettings.PARAM_REGATTA_NAME)
                && parameterMap.get(LeaderboardUrlSettings.PARAM_REGATTA_NAME).size() == 1) {
            result = new RegattaNameAndRaceName(parameterMap.get(LeaderboardUrlSettings.PARAM_REGATTA_NAME).get(0),
                    parameterMap.get(LeaderboardUrlSettings.PARAM_RACE_NAME).get(0));
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public void updatedLeaderboard(LeaderboardDTO leaderboard) {
        // TODO Auto-generated method stub

    }



}