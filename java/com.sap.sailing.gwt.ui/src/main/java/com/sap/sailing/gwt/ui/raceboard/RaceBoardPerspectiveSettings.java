package com.sap.sailing.gwt.ui.raceboard;

import com.sap.sse.common.Duration;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.gwt.shared.GwtHttpRequestUtils;

/**
 * The settings of the raceboard perspective
 * @author Frank
 *
 */
public class RaceBoardPerspectiveSettings extends AbstractSettings {
    private final boolean showLeaderboard;
    private final boolean showWindChart;
    private final boolean showCompetitorsChart;
    private final String activeCompetitorsFilterSetName;
    private final boolean canReplayDuringLiveRaces;
    private final Duration initialDurationAfterRaceStartInReplay;
    
    public static final String PARAM_VIEW_MODE = "viewMode";
    public static final String PARAM_VIEW_SHOW_LEADERBOARD = "viewShowLeaderboard";
    public static final String PARAM_VIEW_SHOW_NAVIGATION_PANEL = "viewShowNavigationPanel";
    public static final String PARAM_VIEW_SHOW_WINDCHART = "viewShowWindChart";
    public static final String PARAM_VIEW_SHOW_COMPETITORSCHART = "viewShowCompetitorsChart";
    public static final String PARAM_VIEW_SHOW_MAPCONTROLS = "viewShowMapControls";
    public static final String PARAM_VIEW_COMPETITOR_FILTER = "viewCompetitorFilter";
    public static final String PARAM_VIEW_CHART_SUPPORT_ENABLED = "viewChartSupportEnabled";
    public static final String PARAM_CAN_REPLAY_DURING_LIVE_RACES = "canReplayDuringLiveRaces";
    public static final String PARAM_TIME_AFTER_RACE_START_AS_HOURS_COLON_MILLIS_COLON_SECONDS = "t";
    
    public RaceBoardPerspectiveSettings() {
        this(/* activeCompetitorsFilterSetName */null, /* showLeaderboard */true,
        /* showWindChart */false, /* showCompetitorsChart */false, 
        /* canReplayDuringLiveRaces */false, /* initialDurationAfterRaceStartInReplay */ null);
    }

    public RaceBoardPerspectiveSettings(String activeCompetitorsFilterSetName, boolean showLeaderboard,
            boolean showWindChart, boolean showCompetitorsChart, boolean canReplayDuringLiveRaces,
            Duration initialDurationAfterRaceStartInReplay) {
        this.activeCompetitorsFilterSetName = activeCompetitorsFilterSetName;
        this.showLeaderboard = showLeaderboard;
        this.showWindChart = showWindChart;
        this.showCompetitorsChart = showCompetitorsChart;
        this.canReplayDuringLiveRaces = canReplayDuringLiveRaces;
        this.initialDurationAfterRaceStartInReplay = initialDurationAfterRaceStartInReplay;
    }

    public boolean isShowLeaderboard() {
        return showLeaderboard;
    }

    public boolean isShowWindChart() {
        return showWindChart;
    }

    public boolean isShowCompetitorsChart() {
        return showCompetitorsChart;
    }

    public String getActiveCompetitorsFilterSetName() {
        return activeCompetitorsFilterSetName;
    }

    public boolean isCanReplayDuringLiveRaces() {
        return canReplayDuringLiveRaces;
    }

    public static RaceBoardPerspectiveSettings readSettingsFromURL(boolean defaultForViewShowLeaderboard,
            boolean defaultForViewShowWindchart, boolean defaultForViewShowCompetitorsChart,
            String defaultForViewCompetitorFilter, boolean defaultForCanReplayDuringLiveRaces) {
        final boolean showLeaderboard = GwtHttpRequestUtils.getBooleanParameter(PARAM_VIEW_SHOW_LEADERBOARD, defaultForViewShowLeaderboard /* default */);
        final boolean showWindChart = GwtHttpRequestUtils.getBooleanParameter(PARAM_VIEW_SHOW_WINDCHART, defaultForViewShowWindchart /* default */);
        final boolean showCompetitorsChart = GwtHttpRequestUtils.getBooleanParameter(PARAM_VIEW_SHOW_COMPETITORSCHART, defaultForViewShowCompetitorsChart /* default */);
        final String activeCompetitorsFilterSetName = GwtHttpRequestUtils.getStringParameter(PARAM_VIEW_COMPETITOR_FILTER, defaultForViewCompetitorFilter /* default */);
        final boolean canReplayWhileLiveIsPossible = GwtHttpRequestUtils.getBooleanParameter(PARAM_CAN_REPLAY_DURING_LIVE_RACES, defaultForCanReplayDuringLiveRaces /* default */);
        final Duration initialDurationAfterRaceStartInReplay = parseDuration(GwtHttpRequestUtils.getStringParameter(
                PARAM_TIME_AFTER_RACE_START_AS_HOURS_COLON_MILLIS_COLON_SECONDS, null /* default */));
        return new RaceBoardPerspectiveSettings(activeCompetitorsFilterSetName, showLeaderboard, showWindChart,
                showCompetitorsChart, canReplayWhileLiveIsPossible, initialDurationAfterRaceStartInReplay);
    }

    public Duration getInitialDurationAfterRaceStartInReplay() {
        return initialDurationAfterRaceStartInReplay;
    }

    /**
     * Understands [hh:[mm:]]ss and parses into a {@link Duration}. If {@code durationAsString} is {@code null} then
     * so is the result.
     */
    private static Duration parseDuration(String durationAsString) {
        final Duration result;
        if (durationAsString == null) {
            result = null;
        } else {
            long seconds = 0;
            for (final String hhmmss : durationAsString.split(":")) {
                seconds = 60*seconds + Long.valueOf(hhmmss);
            }
            result = new MillisecondsDurationImpl(1000l * seconds);
        }
        return result;
    }
}
