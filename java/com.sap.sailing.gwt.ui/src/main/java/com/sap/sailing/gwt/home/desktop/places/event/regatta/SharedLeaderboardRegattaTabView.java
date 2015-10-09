package com.sap.sailing.gwt.home.desktop.places.event.regatta;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.home.desktop.utils.EventParamUtils;
import com.sap.sailing.gwt.ui.client.LeaderboardUpdateListener;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardUrlSettings;
import com.sap.sse.gwt.client.mutationobserver.ElementSizeMutationObserver;
import com.sap.sse.gwt.client.mutationobserver.ElementSizeMutationObserver.DomMutationCallback;
import com.sap.sse.gwt.shared.GwtHttpRequestUtils;

/**
 * An abstract regatta tabView with some shared functions between the leaderboard tab and competitors chart tab 
 */
public abstract class SharedLeaderboardRegattaTabView<T extends AbstractEventRegattaPlace> extends Composite implements RegattaTabView<T>,
        LeaderboardUpdateListener {
    private boolean initialLeaderboardSizeCalculated = false;
    
    public SharedLeaderboardRegattaTabView() {
    }

    public LeaderboardPanel createSharedLeaderboardPanel(String leaderboardName, RegattaAnalyticsDataManager regattaAnalyticsManager) {
        boolean autoExpandLastRaceColumn = GwtHttpRequestUtils.getBooleanParameter(
                LeaderboardUrlSettings.PARAM_AUTO_EXPAND_LAST_RACE_COLUMN, false);
        final LeaderboardSettings leaderboardSettings = EventParamUtils
                .createLeaderboardSettingsFromURLParameters(Window.Location.getParameterMap());
        final RegattaAndRaceIdentifier preselectedRace = EventParamUtils.getPreselectedRace(Window.Location.getParameterMap());
        final LeaderboardPanel leaderboardPanel = regattaAnalyticsManager.createLeaderboardPanel( //
                leaderboardSettings, //
                preselectedRace, //
                "leaderboardGroupName", // TODO: keep using magic string? ask frank!
                leaderboardName, //
                true,
                autoExpandLastRaceColumn);
        leaderboardPanel.addLeaderboardUpdateListener(this);
        
        if(ElementSizeMutationObserver.isSupported()) {
            ElementSizeMutationObserver observer = new ElementSizeMutationObserver(new DomMutationCallback() {
                @Override
                public void onSizeChanged(int newWidth, int newHeight) {
                    if(newWidth > 0 && newHeight > 0 && newWidth > 1500 && initialLeaderboardSizeCalculated == false) {
                        int numberOfLastRacesToShow = (1500 - 600) / 50;
                        leaderboardPanel.setRaceColumnSelectionToLastNStrategy(numberOfLastRacesToShow);
                        initialLeaderboardSizeCalculated = true;
                    }
                }
            }); 
            observer.observe(leaderboardPanel.getLeaderboardTable().getElement());
        }

        return leaderboardPanel;
    }
}