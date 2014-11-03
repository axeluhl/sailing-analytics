package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.user.client.Window;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sse.gwt.client.URLEncoder;

public class LeaderboardUrlSettings {
    public static final String PARAM_LEADERBOARD_GROUP_NAME = "leaderboardGroupName";
    public static final String PARAM_EVENT_ID = "eventId";
    public static final String PARAM_EMBEDDED = "embedded";
    public static final String PARAM_HIDE_TOOLBAR = "hideToolbar";
    public static final String PARAM_SHOW_RACE_DETAILS = "showRaceDetails";
    public static final String PARAM_RACE_NAME = "raceName";
    public static final String PARAM_RACE_DETAIL = "raceDetail";
    public static final String PARAM_OVERALL_DETAIL = "overallDetail";
    public static final String PARAM_LEG_DETAIL = "legDetail";
    public static final String PARAM_MANEUVER_DETAIL = "maneuverDetail";
    public static final String PARAM_AUTO_EXPAND_PRESELECTED_RACE = "autoExpandPreselectedRace";
    public static final String PARAM_AUTO_EXPAND_LAST_RACE_COLUMN = "autoExpandLastRaceColumn";
    public static final String PARAM_REGATTA_NAME = "regattaName";
    public static final String PARAM_REFRESH_INTERVAL_MILLIS = "refreshIntervalMillis";
    public static final String PARAM_SHOW_CHARTS = "showCharts";
    public static final String PARAM_CHART_DETAIL = "chartDetail";
    public static final String PARAM_SHOW_OVERALL_LEADERBOARD = "showOverallLeaderboard";
    public static final String PARAM_SHOW_SERIES_LEADERBOARDS = "showSeriesLeaderboards";
    public static final String PARAM_SHOW_ADDED_SCORES = "showAddedScores";
    public static final String PARAM_SHOW_OVERALL_COLUMN_WITH_NUMBER_OF_RACES_COMPLETED = "showNumberOfRacesCompleted";
    
    /**
     * Parameter to support scaling the complete page by a given factor. This works by either using the
     * CSS3 zoom property or by applying scale operation to the body element. This comes in handy
     * when having to deal with screens that have high resolutions and that can't be controlled manually.
     * It is also a very simple method of adapting the viewport to a tv resolution. This parameter works
     * with value from 0.0 to 10.0 where 1.0 denotes the unchanged level (100%).
     */
    public static final String PARAM_ZOOM_TO = "zoomTo";
    
    /**
     * Lets the client choose a different race column selection which displays only up to the last N races with N being the integer
     * number specified by the parameter.
     */
    public static final String PARAM_NAME_LAST_N = "lastN";

    private final LeaderboardSettings leaderboardSettings;
    private final boolean embedded;
    private final boolean hideToolbar;
    private final boolean showRaceDetails;
    private final boolean autoExpandLastRaceColumn;
    private final boolean autoRefresh;
    private final boolean showCharts;
    private final DetailType chartDetail;
    private final boolean showOverallLeaderboard;
    private final boolean showSeriesLeaderboards;
    
    public LeaderboardUrlSettings(LeaderboardSettings leaderboardSettings, boolean embedded,
            boolean hideToolbar, boolean showRaceDetails, boolean autoRefresh, boolean autoExpandLastRaceColumn,
            boolean showCharts, DetailType chartDetail, boolean showOverallLeaderboard, boolean showSeriesLeaderboards) {
        super();
        this.leaderboardSettings = leaderboardSettings;
        this.embedded = embedded;
        this.hideToolbar = hideToolbar;
        this.showRaceDetails = showRaceDetails;
        this.autoRefresh = autoRefresh;
        this.autoExpandLastRaceColumn = autoExpandLastRaceColumn;
        this.showCharts = showCharts;
        this.chartDetail = chartDetail;
        this.showOverallLeaderboard = showOverallLeaderboard;
        this.showSeriesLeaderboards = showSeriesLeaderboards;
    }

    public LeaderboardSettings getLeaderboardSettings() {
        return leaderboardSettings;
    }

    public boolean isEmbedded() {
        return embedded;
    }

    public boolean isShowRaceDetails() {
        return showRaceDetails;
    }

    public boolean isAutoRefresh() {
        return autoRefresh;
    }

    public boolean isAutoExpandLastRaceColumn() {
        return autoExpandLastRaceColumn;
    }

    public boolean isHideToolbar() {
        return hideToolbar;
    }

    public boolean isShowCharts() {
        return showCharts;
    }

    public DetailType getChartDetail() {
        return chartDetail;
    }

    public boolean isShowOverallLeaderboard() {
        return showOverallLeaderboard;
    }

    public boolean isShowSeriesLeaderboards() {
        return showSeriesLeaderboards;
    }
    
    /**
     * Assembles a URL for a leaderboard that displays with the <code>settings</code> and <code>embedded</code> mode
     * as specified by the parameters.
     */
    public static String getUrl(String leaderboardName, String leaderboardDisplayName, LeaderboardUrlSettings settings) {
        StringBuilder overallDetails = new StringBuilder();
        for (DetailType overallDetail : settings.getLeaderboardSettings().getOverallDetailsToShow()) {
            overallDetails.append('&');
            overallDetails.append(LeaderboardUrlSettings.PARAM_OVERALL_DETAIL);
            overallDetails.append('=');
            overallDetails.append(overallDetail.name());
        }
        StringBuilder legDetails = new StringBuilder();
        for (DetailType legDetail : settings.getLeaderboardSettings().getLegDetailsToShow()) {
            legDetails.append('&');
            legDetails.append(LeaderboardUrlSettings.PARAM_LEG_DETAIL);
            legDetails.append('=');
            legDetails.append(legDetail.name());
        }
        StringBuilder raceDetails = new StringBuilder();
        for (DetailType raceDetail : settings.getLeaderboardSettings().getRaceDetailsToShow()) {
            raceDetails.append('&');
            raceDetails.append(LeaderboardUrlSettings.PARAM_RACE_DETAIL);
            raceDetails.append('=');
            raceDetails.append(raceDetail.name());
        }
        StringBuilder maneuverDetails = new StringBuilder();
        for (DetailType maneuverDetail : settings.getLeaderboardSettings().getManeuverDetailsToShow()) {
            maneuverDetails.append('&');
            maneuverDetails.append(LeaderboardUrlSettings.PARAM_MANEUVER_DETAIL);
            maneuverDetails.append('=');
            maneuverDetails.append(maneuverDetail.name());
        }
        StringBuilder showAddedScores = new StringBuilder();
        showAddedScores.append('&');
        showAddedScores.append(LeaderboardUrlSettings.PARAM_SHOW_ADDED_SCORES);
        showAddedScores.append('=');
        showAddedScores.append(settings.getLeaderboardSettings().isShowAddedScores());

        String debugParam = Window.Location.getParameter("gwt.codesvr");
        String link = URLEncoder.encode("/gwt/Leaderboard.html?name=" + leaderboardName
                + (settings.isShowRaceDetails() ? "&"+LeaderboardUrlSettings.PARAM_SHOW_RACE_DETAILS+"=true" : "")
                + (leaderboardDisplayName != null ? "&displayName="+leaderboardDisplayName : "")
                + (settings.isEmbedded() ? "&"+LeaderboardUrlSettings.PARAM_EMBEDDED+"=true" : "")
                + (settings.isHideToolbar() ? "&"+LeaderboardUrlSettings.PARAM_HIDE_TOOLBAR+"=true" : "")
                + (settings.isShowCharts() ? "&"+LeaderboardUrlSettings.PARAM_SHOW_CHARTS+"=true" : "")
                + (settings.isShowCharts() ? "&"+LeaderboardUrlSettings.PARAM_CHART_DETAIL+"="+settings.getChartDetail().name() : "")
                + (settings.isShowOverallLeaderboard() ? "&"+LeaderboardUrlSettings.PARAM_SHOW_OVERALL_LEADERBOARD+"=true" : "")
                + (settings.isShowSeriesLeaderboards() ? "&"+LeaderboardUrlSettings.PARAM_SHOW_SERIES_LEADERBOARDS+"=true" : "")
                + (!settings.isAutoRefresh() || (settings.getLeaderboardSettings().getDelayBetweenAutoAdvancesInMilliseconds() == null &&
                   settings.getLeaderboardSettings().getDelayBetweenAutoAdvancesInMilliseconds() != 0) ? "" :
                    "&"+LeaderboardUrlSettings.PARAM_REFRESH_INTERVAL_MILLIS+"="+settings.getLeaderboardSettings().getDelayBetweenAutoAdvancesInMilliseconds())
                + legDetails.toString()
                + raceDetails.toString()
                + overallDetails.toString()
                + maneuverDetails.toString()
                + (settings.isAutoExpandLastRaceColumn() ? "&"+LeaderboardUrlSettings.PARAM_AUTO_EXPAND_LAST_RACE_COLUMN+"=true" : "")
                + (settings.getLeaderboardSettings().getNumberOfLastRacesToShow() == null ? "" :
                    "&"+LeaderboardUrlSettings.PARAM_NAME_LAST_N+"="+settings.getLeaderboardSettings().getNumberOfLastRacesToShow())
                + showAddedScores.toString()
                + (debugParam != null && !debugParam.isEmpty() ? "&gwt.codesvr=" + debugParam : ""));
        return link;
    }
}

