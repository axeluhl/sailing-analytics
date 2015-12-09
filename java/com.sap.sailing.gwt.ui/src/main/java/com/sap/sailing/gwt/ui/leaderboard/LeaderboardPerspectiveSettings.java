package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sse.common.settings.AbstractSettings;

/**
 * Represents the parameters for configuring the standalone leaderboard view
 * 
 * @author Frank
 *
 */
public class LeaderboardPerspectiveSettings extends AbstractSettings {
    private final boolean leaderboardAutoZoom;
    private final Double leaderboardZoomFactor;

    public final static String PARAM_LEADEROARD_AUTO_ZOOM = "leaderboardAutoZoom"; 
    public final static String PARAM_LEADEROARD_ZOOM_FACTOR = "leaderboardZoomFactor"; 

    public LeaderboardPerspectiveSettings() {
        this(true, 1.0);
    }

    public LeaderboardPerspectiveSettings(boolean leaderboardAutoZoom, Double leaderboardZoomFactor) {
        this.leaderboardAutoZoom = leaderboardAutoZoom;
        this.leaderboardZoomFactor = leaderboardZoomFactor;
    }

    public boolean isLeaderboardAutoZoom() {
        return leaderboardAutoZoom;
    }

    public Double getLeaderboardZoomFactor() {
        return leaderboardZoomFactor;
    }
}
