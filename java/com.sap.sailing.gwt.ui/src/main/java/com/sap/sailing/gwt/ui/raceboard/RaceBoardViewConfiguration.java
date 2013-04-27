package com.sap.sailing.gwt.ui.raceboard;

/** 
 * Represents the parameters for configuring the raceboard view
 * @author Frank
 *
 */
public class RaceBoardViewConfiguration {
    private boolean showLeaderboard; 
    private boolean showWindChart; 
    private boolean showCompetitorsChart;
    private ViewModes viewMode;
    private String activeCompetitorsFilterSetName;
    
    public static final String PARAM_VIEW_MODE = "viewMode";
    public static final String PARAM_VIEW_SHOW_LEADERBOARD = "viewShowLeaderboard";
    public static final String PARAM_VIEW_SHOW_NAVIGATION_PANEL = "viewShowNavigationPanel";
    public static final String PARAM_VIEW_SHOW_WINDCHART = "viewShowWindChart";
    public static final String PARAM_VIEW_SHOW_COMPETITORSCHART = "viewShowCompetitorsChart";
    public static final String PARAM_VIEW_COMPETITOR_FILTER = "viewCompetitorFilter";

    public static enum ViewModes { ONESCREEN };
    
    public RaceBoardViewConfiguration() {
        viewMode = ViewModes.ONESCREEN;
        showLeaderboard = true;
        showWindChart = false;
        showCompetitorsChart = false;
    }
    
    public RaceBoardViewConfiguration(ViewModes viewMode, String activeCompetitorsFilterSetName,
            boolean showLeaderboard, boolean showWindChart, boolean showCompetitorsChart) {
        this.viewMode = viewMode;
        this.activeCompetitorsFilterSetName = activeCompetitorsFilterSetName;
        this.showLeaderboard = showLeaderboard;
        this.showWindChart = showWindChart;
        this.showCompetitorsChart = showCompetitorsChart;
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

    public ViewModes getViewMode() {
        return viewMode;
    }

    public String getActiveCompetitorsFilterSetName() {
        return activeCompetitorsFilterSetName;
    }
}
