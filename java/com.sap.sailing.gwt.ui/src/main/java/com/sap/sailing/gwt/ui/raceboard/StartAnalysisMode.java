package com.sap.sailing.gwt.ui.raceboard;

import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sse.gwt.client.player.Timer.PlayStates;

/**
 * The start analysis mode makes the competitor chart visible and sets it to speed over ground; the
 * {@link LeaderboardSettings} are adjusted such that no leg columns but only start parameters are
 * shown. The top three starters are selected when the leaderboard has been updated after setting
 * the timer to a few seconds after the start. The {@link PlayStates#Paused} is used for the timer.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class StartAnalysisMode implements RaceBoardMode {
    @Override
    public void applyTo(RaceBoardPanel raceBoardPanel) {
        // TODO Auto-generated method stub
    }
}
