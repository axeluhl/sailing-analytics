package com.sap.sailing.gwt.home.client.place.event.regatta.tabs;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.home.client.place.event.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.client.place.event.regatta.RegattaAnalyticsDataManager;
import com.sap.sailing.gwt.home.client.place.event.regatta.RegattaTabView;
import com.sap.sailing.gwt.home.client.place.event.utils.EventParamUtils;
import com.sap.sailing.gwt.ui.client.LeaderboardUpdateListener;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardUrlSettings;
import com.sap.sse.gwt.shared.GwtHttpRequestUtils;

/**
 * A regatta tab view which some shared functions between leaderboard tab and competitor 
 */
public abstract class SharedLeaderboardRegattaTabView<T extends AbstractEventRegattaPlace> extends Composite implements RegattaTabView<T>,
        LeaderboardUpdateListener {

    public SharedLeaderboardRegattaTabView() {

    }

    public LeaderboardPanel createSharedLeaderboardPanel(String leaderboardName, RegattaAnalyticsDataManager regattaAnalyticsManager) {
        boolean autoExpandLastRaceColumn = GwtHttpRequestUtils.getBooleanParameter(
                LeaderboardUrlSettings.PARAM_AUTO_EXPAND_LAST_RACE_COLUMN, false);
        final LeaderboardSettings leaderboardSettings = EventParamUtils
                .createLeaderboardSettingsFromURLParameters(Window.Location.getParameterMap());
        final RegattaAndRaceIdentifier preselectedRace = EventParamUtils.getPreselectedRace(Window.Location.getParameterMap());
        LeaderboardPanel leaderboardPanel = regattaAnalyticsManager.createLeaderboardPanel( //
                leaderboardSettings, //
                preselectedRace, //
                "leaderboardGroupName", // TODO: keep using magic string? ask frank!
                leaderboardName, //
                true,
                autoExpandLastRaceColumn);
        leaderboardPanel.addLeaderboardUpdateListener(this);
        return leaderboardPanel;
    }
}