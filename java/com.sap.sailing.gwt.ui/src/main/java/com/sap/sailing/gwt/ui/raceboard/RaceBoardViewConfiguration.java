package com.sap.sailing.gwt.ui.raceboard;

import com.sap.sse.common.Duration;

/** 
 * Represents the parameters for configuring the raceboard view
 * @author Frank
 *
 */
public class RaceBoardViewConfiguration {
    private final boolean showLeaderboard; 
    private final boolean showWindChart; 
    private final boolean showCompetitorsChart;
    private final String activeCompetitorsFilterSetName;
    private final boolean autoSelectMedia;
    private final String defaultMedia;
    private final boolean showViewStreamlets;
    private final boolean showViewStreamletColors;
    private final boolean showViewSimulation;
    private final Duration initialDurationAfterRaceStartInReplay;
    
    public static final String PARAM_VIEW_MODE = "viewMode";
    public static final String PARAM_VIEW_SHOW_LEADERBOARD = "viewShowLeaderboard";
    public static final String PARAM_VIEW_SHOW_NAVIGATION_PANEL = "viewShowNavigationPanel";
    public static final String PARAM_VIEW_SHOW_WINDCHART = "viewShowWindChart";
    public static final String PARAM_VIEW_SHOW_COMPETITORSCHART = "viewShowCompetitorsChart";
    public static final String PARAM_VIEW_SHOW_MAPCONTROLS = "viewShowMapControls";
    public static final String PARAM_VIEW_SHOW_STREAMLETS = "viewShowStreamlets";
    public static final String PARAM_VIEW_SHOW_STREAMLET_COLORS = "viewShowStreamletColors";
    public static final String PARAM_VIEW_SHOW_SIMULATION = "viewShowSimulation";
    public static final String PARAM_VIEW_COMPETITOR_FILTER = "viewCompetitorFilter";
    public static final String PARAM_CAN_REPLAY_DURING_LIVE_RACES = "canReplayDuringLiveRaces";
    public static final String PARAM_AUTOSELECT_MEDIA = "autoSelectMedia";
    public static final String PARAM_DEFAULT_MEDIA = "defaultMedia";
    public static final String PARAM_TIME_AFTER_RACE_START_AS_HOURS_COLON_MILLIS_COLON_SECONDS = "t";

    public RaceBoardViewConfiguration() {
        this(/* activeCompetitorsFilterSetName */ null, /* showLeaderboard */ true,
                /* showWindChart */ false, /* showCompetitorsChart */ false, /* showViewStreamlets */ false, /* showViewStreamletColors */ false, /* showViewSimulation */ false,
         /* autoSelectMedia */false, null, /* initialDurationAfterRaceStartInReplay */ null);
    }	
    
    public RaceBoardViewConfiguration(String activeCompetitorsFilterSetName, boolean showLeaderboard,
            boolean showWindChart, boolean showCompetitorsChart, boolean showViewStreamlets, boolean showViewStreamletColors, boolean showViewSimulation,
            boolean autoSelectMedia, String defaultMedia, Duration initialDurationAfterRaceStartInReplay) {
        this.activeCompetitorsFilterSetName = activeCompetitorsFilterSetName;
        this.showLeaderboard = showLeaderboard;
        this.showWindChart = showWindChart;
        this.showCompetitorsChart = showCompetitorsChart;
        this.showViewStreamlets = showViewStreamlets;
        this.showViewStreamletColors = showViewStreamletColors;
        this.showViewSimulation = showViewSimulation;
        this.autoSelectMedia = autoSelectMedia;
        this.defaultMedia = defaultMedia;
        this.initialDurationAfterRaceStartInReplay = initialDurationAfterRaceStartInReplay;
    }

    public boolean isShowLeaderboard() {
        return showLeaderboard;
    }

    public boolean isShowWindChart() {
        return showWindChart;
    }

    public boolean isShowViewStreamlets() {
        return showViewStreamlets;
    }

    public boolean isShowViewStreamletColors() {
        return showViewStreamletColors;
    }

    public boolean isShowViewSimulation() {
        return showViewSimulation;
    }

    public boolean isShowCompetitorsChart() {
        return showCompetitorsChart;
    }

    public String getActiveCompetitorsFilterSetName() {
        return activeCompetitorsFilterSetName;
    }

    public boolean isAutoSelectMedia() {
        return autoSelectMedia;
    }

    public String getDefaultMedia() {
        return defaultMedia;
    }

    public Duration getInitialDurationAfterRaceStartInReplay() {
        return initialDurationAfterRaceStartInReplay;
    }
}
