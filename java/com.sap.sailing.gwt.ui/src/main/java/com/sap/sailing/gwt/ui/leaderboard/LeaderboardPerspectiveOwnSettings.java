package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.BooleanSetting;
import com.sap.sse.common.settings.generic.StringSetting;

public class LeaderboardPerspectiveOwnSettings extends AbstractGenericSerializableSettings {
    private static final long serialVersionUID = 8978956250485028963L;
    
    private transient BooleanSetting showRaceDetails;
    private transient BooleanSetting hideToolbar;
    private transient BooleanSetting autoExpandLastRaceColumn;
    private transient BooleanSetting showCharts;
    private transient BooleanSetting showOverallLeaderboard;
    private transient BooleanSetting showSeriesLeaderboards;
    private transient StringSetting zoomTo;
    
    LeaderboardPerspectiveOwnSettings() {

    }

    LeaderboardPerspectiveOwnSettings(boolean showRaceDetails, boolean hideToolbar, boolean autoExpandLastRaceColumn,
            boolean showCharts, boolean showOverallLeaderboard, boolean showSeriesLeaderboards, String zoomTo) {
        this.showRaceDetails.setValue(showRaceDetails);
        this.hideToolbar.setValue(hideToolbar);
        this.autoExpandLastRaceColumn.setValue(autoExpandLastRaceColumn);
        this.showCharts.setValue(showCharts);
        this.showOverallLeaderboard.setValue(showOverallLeaderboard);
        this.showSeriesLeaderboards.setValue(showSeriesLeaderboards);
        this.zoomTo.setValue(zoomTo);
    }

    @Override
    protected void addChildSettings() {
        showRaceDetails = new BooleanSetting(LeaderboardUrlSettings.PARAM_SHOW_RACE_DETAILS, this, false);
        hideToolbar = new BooleanSetting(LeaderboardUrlSettings.PARAM_HIDE_TOOLBAR, this, false);
        autoExpandLastRaceColumn = new BooleanSetting(LeaderboardUrlSettings.PARAM_AUTO_EXPAND_LAST_RACE_COLUMN, this, false);
        showCharts = new BooleanSetting(LeaderboardUrlSettings.PARAM_SHOW_CHARTS, this, false);
        showOverallLeaderboard = new BooleanSetting(LeaderboardUrlSettings.PARAM_SHOW_OVERALL_LEADERBOARD, this, false);
        showSeriesLeaderboards = new BooleanSetting(LeaderboardUrlSettings.PARAM_SHOW_SERIES_LEADERBOARDS, this, false);
        zoomTo = new StringSetting(LeaderboardUrlSettings.PARAM_ZOOM_TO, this, "1");
    }
    public boolean isShowRaceDetails() {
        return showRaceDetails.getValue();
    }
    public boolean isHideToolbar() {
        return hideToolbar.getValue();
    }
    public boolean isAutoExpandLastRaceColumn() {
        return autoExpandLastRaceColumn.getValue();
    }
    public boolean isShowCharts() {
        return showCharts.getValue();
    }
    public boolean isShowOverallLeaderboard() {
        return showOverallLeaderboard.getValue();
    }
    public boolean isShowSeriesLeaderboards() {
        return showSeriesLeaderboards.getValue();
    }
    public String getZoomTo() {
        return zoomTo.getValue();
    }
}
