package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.dom.client.Document;
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
    private final boolean simulationEnabled;
    
    /** indicates whether it's enabled to display charts or not */
    private final boolean chartSupportEnabled;

    public static final String PARAM_VIEW_MODE = "viewMode";
    public static final String PARAM_VIEW_SHOW_LEADERBOARD = "viewShowLeaderboard";
    public static final String PARAM_VIEW_SHOW_NAVIGATION_PANEL = "viewShowNavigationPanel";
    public static final String PARAM_VIEW_SHOW_WINDCHART = "viewShowWindChart";
    public static final String PARAM_VIEW_SHOW_COMPETITORSCHART = "viewShowCompetitorsChart";
    public static final String PARAM_VIEW_SIMULATION_ENABLED = "viewSimulationEnabled";
    public static final String PARAM_VIEW_COMPETITOR_FILTER = "viewCompetitorFilter";
    public static final String PARAM_VIEW_CHART_SUPPORT_ENABLED = "viewChartSupportEnabled";
    public static final String PARAM_CAN_REPLAY_DURING_LIVE_RACES = "canReplayDuringLiveRaces";
    
    public RaceBoardPerspectiveSettings() {
        this(/* activeCompetitorsFilterSetName */null, /* showLeaderboard */true,
        /* showWindChart */false, /* showCompetitorsChart */false, 
        /* simulationEnabled */false, /* canReplayDuringLiveRaces */false, /* chartSupportEnabled */ true);
    }

    public RaceBoardPerspectiveSettings(String activeCompetitorsFilterSetName, boolean showLeaderboard,
            boolean showWindChart, boolean showCompetitorsChart, boolean simulationEnabled, boolean canReplayDuringLiveRaces,
            boolean chartSupportEnabled) {
        this.activeCompetitorsFilterSetName = activeCompetitorsFilterSetName;
        this.showLeaderboard = showLeaderboard;
        this.showWindChart = showWindChart;
        this.showCompetitorsChart = showCompetitorsChart;
        this.simulationEnabled = simulationEnabled;
        this.canReplayDuringLiveRaces = canReplayDuringLiveRaces;
        this.chartSupportEnabled = chartSupportEnabled;
    }

    public boolean isShowLeaderboard() {
        return showLeaderboard;
    }

    public boolean isShowWindChart() {
        return showWindChart;
    }

    public boolean isSimulationEnabled() {
        return simulationEnabled;
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

    public boolean isChartSupportEnabled() {
        return chartSupportEnabled;
    }

    public static RaceBoardPerspectiveSettings readSettingsFromURL() {
        final boolean showLeaderboard = GwtHttpRequestUtils.getBooleanParameter(PARAM_VIEW_SHOW_LEADERBOARD, true /* default */);
        final boolean showWindChart = GwtHttpRequestUtils.getBooleanParameter(PARAM_VIEW_SHOW_WINDCHART, false /* default */);
        final boolean simulationEnabled = GwtHttpRequestUtils.getBooleanParameter(PARAM_VIEW_SIMULATION_ENABLED, false /* default */);
        final boolean showCompetitorsChart = GwtHttpRequestUtils.getBooleanParameter(PARAM_VIEW_SHOW_COMPETITORSCHART, false /* default */);
        String activeCompetitorsFilterSetName = GwtHttpRequestUtils.getStringParameter(PARAM_VIEW_COMPETITOR_FILTER, null /* default */);
        final boolean canReplayWhileLiveIsPossible = GwtHttpRequestUtils.getBooleanParameter(PARAM_CAN_REPLAY_DURING_LIVE_RACES, false /* default */);
        
        // Determine if the screen is large enough to display charts such as the competitor chart or the wind chart.
        // This decision is made once based on the initial screen height. Resizing the window afterwards will have
        // no impact on the chart support, i.e. they are available/unavailable based on the initial decision.
        boolean isScreenLargeEnoughToOfferChartSupport = Document.get().getClientHeight() >= 600;
        final boolean chartSupportEnabled = GwtHttpRequestUtils.getBooleanParameter(PARAM_VIEW_CHART_SUPPORT_ENABLED, isScreenLargeEnoughToOfferChartSupport);

        return new RaceBoardPerspectiveSettings(activeCompetitorsFilterSetName, showLeaderboard, showWindChart,
                showCompetitorsChart, simulationEnabled, canReplayWhileLiveIsPossible, chartSupportEnabled);
    }
}
