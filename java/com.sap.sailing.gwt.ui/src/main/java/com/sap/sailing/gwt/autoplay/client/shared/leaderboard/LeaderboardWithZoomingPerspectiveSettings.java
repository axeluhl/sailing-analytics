package com.sap.sailing.gwt.autoplay.client.shared.leaderboard;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.BooleanSetting;
import com.sap.sse.common.settings.generic.DoubleSetting;
import com.sap.sse.security.ui.client.SecurityChildSettingsContext;

/**
 * Represents the parameters for configuring a standalone leaderboard with a header view
 * 
 * @author Frank
 *
 */
public class LeaderboardWithZoomingPerspectiveSettings extends AbstractGenericSerializableSettings<SecurityChildSettingsContext> {
    
    private static final long serialVersionUID = 69425582611507634L;
    
    private BooleanSetting leaderboardAutoZoom;
    private DoubleSetting leaderboardZoomFactor;

    public final static String PARAM_LEADEROARD_AUTO_ZOOM = "leaderboardAutoZoom"; 
    public final static String PARAM_LEADEROARD_ZOOM_FACTOR = "leaderboardZoomFactor";
    
    @Override
    protected void addChildSettings(SecurityChildSettingsContext context) {
        leaderboardAutoZoom = new BooleanSetting("leaderboardAutoZoom", this, true);
        leaderboardZoomFactor = new DoubleSetting("leaderboardZoomFactor", this, 1.0);
    }

    public LeaderboardWithZoomingPerspectiveSettings() {
        super(null);
    }

    public LeaderboardWithZoomingPerspectiveSettings(boolean leaderboardAutoZoom, Double leaderboardZoomFactor) {
        this();
        this.leaderboardAutoZoom.setValue(leaderboardAutoZoom);
        this.leaderboardZoomFactor.setValue(leaderboardZoomFactor);
    }

    public boolean isLeaderboardAutoZoom() {
        return leaderboardAutoZoom.getValue();
    }

    public Double getLeaderboardZoomFactor() {
        return leaderboardZoomFactor.getValue();
    }
}
