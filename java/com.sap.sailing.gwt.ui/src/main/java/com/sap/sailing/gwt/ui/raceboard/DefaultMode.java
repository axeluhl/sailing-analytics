package com.sap.sailing.gwt.ui.raceboard;

import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.gwt.client.player.Timer.PlayModes;

/**
 * This is the default mode used when no mode is explicitly given.
 */
public class DefaultMode extends AbstractRaceBoardMode {
    
    private boolean timerAdjusted;
    
    @Override
    protected void trigger() {
        if (getTimer().getPlayMode() == PlayModes.Live) {
            stopReceivingRaceTimesInfos(); // this trigger wouldn't be stopped otherwise
        }
        if (!timerAdjusted && getTimer().getPlayMode() != PlayModes.Live && getRaceTimesInfoForRace() != null && getRaceTimesInfoForRace().getStartOfTracking() != null) {
            timerAdjusted = true;
            // If the time isn't explicitly set, the time slider isn't initialized and directly jumps to the end when
            // pressing the play button.
            getTimer().setTime(new MillisecondsTimePoint(getRaceTimesInfoForRace().getStartOfTracking()).asMillis());
            // we've done our adjustments; remove listener and let go
            stopReceivingRaceTimesInfos();
        }
        // We don't care about the leaderboard in this mode
        stopReceivingLeaderboard();
    }
}
