package com.sap.sailing.gwt.home.client.place.fakeseries.tabs;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.home.client.place.event.utils.EventParamUtils;
import com.sap.sailing.gwt.home.client.place.fakeseries.AbstractSeriesTabPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.EventSeriesAnalyticsDataManager;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesTabView;
import com.sap.sailing.gwt.ui.client.LeaderboardUpdateListener;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardUrlSettings;
import com.sap.sse.gwt.shared.GwtHttpRequestUtils;

/**
 * An abstract series tabView with some shared functions between the overall leaderboard tab and competitors chart tab 
 */
public abstract class SharedLeaderboardEventSeriesTabView<T extends AbstractSeriesTabPlace> extends Composite implements SeriesTabView<T>,
        LeaderboardUpdateListener {

    public SharedLeaderboardEventSeriesTabView() {
    }
    
    public LeaderboardPanel createSharedLeaderboardPanel(String leaderboardName, EventSeriesAnalyticsDataManager eventSeriesAnalyticsManager) {
        boolean autoExpandLastRaceColumn = GwtHttpRequestUtils.getBooleanParameter(
                LeaderboardUrlSettings.PARAM_AUTO_EXPAND_LAST_RACE_COLUMN, false);
        final LeaderboardSettings leaderboardSettings = EventParamUtils
                .createLeaderboardSettingsFromURLParameters(Window.Location
                .getParameterMap());
        final RegattaAndRaceIdentifier preselectedRace = EventParamUtils
                .getPreselectedRace(Window.Location.getParameterMap());
        LeaderboardPanel leaderboardPanel = eventSeriesAnalyticsManager.createOverallLeaderboardPanel(
                leaderboardSettings,
                preselectedRace,
                "leaderboardGroupName",
                leaderboardName,
                true, // this information came from place, now hard coded. check with frank
                autoExpandLastRaceColumn);
        leaderboardPanel.addLeaderboardUpdateListener(this);
        return leaderboardPanel;
    }
}
    