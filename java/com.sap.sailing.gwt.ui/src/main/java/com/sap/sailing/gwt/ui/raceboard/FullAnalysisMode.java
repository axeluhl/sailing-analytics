package com.sap.sailing.gwt.ui.raceboard;

import com.sap.sse.gwt.client.player.Timer.PlayStates;

/**
 * This mode best applies to non-live "replay" races. It sets the time to the "end of race" time point and makes sure
 * the race column in the leaderboard is expanded. The timer is put into {@link PlayStates#Paused} mode.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class FullAnalysisMode implements RaceBoardMode {
    @Override
    public void applyTo(RaceBoardPanel raceBoardPanel) {
        // TODO Auto-generated method stub
    }
}
