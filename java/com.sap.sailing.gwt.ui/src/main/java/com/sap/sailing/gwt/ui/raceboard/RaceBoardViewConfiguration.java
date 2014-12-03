package com.sap.sailing.gwt.ui.raceboard;

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
    private final boolean canReplayDuringLiveRaces;
    private final boolean autoSelectMedia;
    private final String defaultMedia;
    private final boolean showViewStreamlets;
    private final boolean showViewSimulation;
    
    public static final String PARAM_VIEW_MODE = "viewMode";
    public static final String PARAM_VIEW_SHOW_LEADERBOARD = "viewShowLeaderboard";
    public static final String PARAM_VIEW_SHOW_NAVIGATION_PANEL = "viewShowNavigationPanel";
    public static final String PARAM_VIEW_SHOW_WINDCHART = "viewShowWindChart";
    public static final String PARAM_VIEW_SHOW_COMPETITORSCHART = "viewShowCompetitorsChart";
    public static final String PARAM_VIEW_SHOW_MAPCONTROLS = "viewShowMapControls";
    public static final String PARAM_VIEW_SHOW_STREAMLETS = "viewShowStreamlets";
    public static final String PARAM_VIEW_SHOW_SIMULATION = "viewShowSimulation";
    public static final String PARAM_VIEW_COMPETITOR_FILTER = "viewCompetitorFilter";
    public static final String PARAM_CAN_REPLAY_DURING_LIVE_RACES = "canReplayDuringLiveRaces";
    public static final String PARAM_AUTOSELECT_MEDIA = "autoSelectMedia";
    public static final String PARAM_DEFAULT_MEDIA = "defaultMedia";

    public RaceBoardViewConfiguration() {
        this(/* activeCompetitorsFilterSetName */ null, /* showLeaderboard */ true,
                /* showWindChart */ false, /* showCompetitorsChart */ false, /* showViewStreamlets */ false, /* showViewSimulation */ false,
        /* canReplayDuringLiveRaces */false, /* autoSelectMedia */false, null);
    }	
    
    public RaceBoardViewConfiguration(String activeCompetitorsFilterSetName, boolean showLeaderboard,
            boolean showWindChart, boolean showCompetitorsChart, boolean showViewStreamlets, boolean showViewSimulation, boolean canReplayDuringLiveRaces,
            boolean autoSelectMedia, String defaultMedia) {
        this.activeCompetitorsFilterSetName = activeCompetitorsFilterSetName;
        this.showLeaderboard = showLeaderboard;
        this.showWindChart = showWindChart;
        this.showCompetitorsChart = showCompetitorsChart;
        this.showViewStreamlets = showViewStreamlets;
        this.showViewSimulation = showViewSimulation;
        this.canReplayDuringLiveRaces = canReplayDuringLiveRaces;
        this.autoSelectMedia = autoSelectMedia;
        this.defaultMedia = defaultMedia;
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

    public boolean isShowViewSimulation() {
        return showViewSimulation;
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

    public boolean isAutoSelectMedia() {
		return autoSelectMedia;
	}

    public String getDefaultMedia() {
        return defaultMedia;
    }

}
