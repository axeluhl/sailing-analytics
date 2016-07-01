package com.sap.sailing.gwt.ui.raceboard;

import java.util.Date;
import java.util.Map;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProviderListener;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sse.common.Duration;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.player.Timer.PlayStates;

/**
 * Puts the race viewer into player mode, setting the timer to 10s before start and into {@link PlayStates#Playing} state if it's
 * not a live race where auto-play is started anyway.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class PlayerMode implements RaceBoardMode, RaceTimesInfoProviderListener {
    private final Duration DURATION_BEFORE_START_TO_SET_TIMER_TO_FOR_REPLAY_RACES = Duration.ONE_SECOND.times(10);
    private Timer timer;
    private RegattaAndRaceIdentifier raceIdentifier;
    
    @Override
    public void applyTo(RaceBoardPanel raceBoardPanel) {
        raceBoardPanel.getRaceTimePanel().addRaceTimesInfoProviderListener(this);
        this.timer = raceBoardPanel.getTimer();
        this.raceIdentifier = raceBoardPanel.getSelectedRaceIdentifier();
    }

    /**
     * Called after the {@link RaceTimePanel} has reacted to this update. We assume that now the timing for the race has been
     * received, and it should be clear by now whether we're talkign about a live or a replay race. In case of a replay race
     * the timer is set to 
     */
    @Override
    public void raceTimesInfosReceived(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo,
            long clientTimeWhenRequestWasSent, Date serverTimeDuringRequest, long clientTimeWhenResponseWasReceived) {
        if (timer.getPlayMode() != PlayModes.Live && !raceTimesInfo.isEmpty() && raceTimesInfo.containsKey(raceIdentifier)) {
            final RaceTimesInfoDTO times = raceTimesInfo.get(raceIdentifier);
            if (times.startOfRace != null) {
                timer.setTime(new MillisecondsTimePoint(times.startOfRace).minus(DURATION_BEFORE_START_TO_SET_TIMER_TO_FOR_REPLAY_RACES).asMillis());
                timer.play();
            }
        }
    }
}
