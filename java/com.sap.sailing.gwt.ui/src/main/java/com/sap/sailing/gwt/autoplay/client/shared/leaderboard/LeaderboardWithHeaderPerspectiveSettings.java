package com.sap.sailing.gwt.autoplay.client.shared.leaderboard;

import com.sap.sse.common.settings.AbstractSettings;

/**
 * Represents the parameters for configuring a standalone leaderboard with a header view
 * 
 * @author Frank
 *
 */
public class LeaderboardWithHeaderPerspectiveSettings extends AbstractSettings {
    private final boolean leaderboardAutoZoom;
    private final Double leaderboardZoomFactor;

    public final static String PARAM_LEADEROARD_AUTO_ZOOM = "leaderboardAutoZoom"; 
    public final static String PARAM_LEADEROARD_ZOOM_FACTOR = "leaderboardZoomFactor"; 

    public LeaderboardWithHeaderPerspectiveSettings() {
        this(true, 1.0);
    }

    public LeaderboardWithHeaderPerspectiveSettings(boolean leaderboardAutoZoom, Double leaderboardZoomFactor) {
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
