package com.sap.sailing.gwt.ui.raceboard;

import java.util.Date;
import java.util.Map;

import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.client.LeaderboardUpdateListener;
import com.sap.sailing.gwt.ui.client.RaceTimePanel;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProviderListener;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceCompetitorSet.CompetitorsForRaceDefinedListener;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sse.gwt.client.player.Timer;

/**
 * Abstract base class for implementing a {@link PlayerMode}. The {@link #applyTo(RaceBoardPanel)} method
 * registers this object as a leaderboard update listener and as a race times info provider listener. The
 * default implementations of the corresponding callback methods will simply unregister this object as a
 * listener. If subclasses want to stick with this behavior they can call the {@code super} implementation
 * in their overrides.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public abstract class AbstractRaceBoardMode implements RaceBoardMode, RaceTimesInfoProviderListener, LeaderboardUpdateListener, CompetitorsForRaceDefinedListener {
    private Timer timer;
    private RegattaAndRaceIdentifier raceIdentifier;
    private RaceTimePanel raceTimePanel;
    private LeaderboardPanel leaderboardPanel;
    private RaceBoardPanel raceBoardPanel;
    
    @Override
    public void applyTo(RaceBoardPanel raceBoardPanel) {
        this.raceBoardPanel = raceBoardPanel;
        this.raceTimePanel = raceBoardPanel.getRaceTimePanel();
        this.raceTimePanel.addRaceTimesInfoProviderListener(this);
        this.leaderboardPanel = raceBoardPanel.getLeaderboardPanel();
        this.leaderboardPanel.addLeaderboardUpdateListener(this);
        this.timer = raceBoardPanel.getTimer();
        this.raceIdentifier = raceBoardPanel.getSelectedRaceIdentifier();
        raceBoardPanel.getMap().addCompetitorsForRaceDefinedListener(this);
    }
    
    protected Timer getTimer() {
        return timer;
    }

    protected RegattaAndRaceIdentifier getRaceIdentifier() {
        return raceIdentifier;
    }

    protected RaceTimePanel getRaceTimePanel() {
        return raceTimePanel;
    }

    protected LeaderboardPanel getLeaderboardPanel() {
        return leaderboardPanel;
    }

    protected RaceBoardPanel getRaceBoardPanel() {
        return raceBoardPanel;
    }

    /**
     * Called after the {@link RaceTimePanel} has reacted to this update. We assume that now the timing for the race has been
     * received, and it should be clear by now whether we're talking about a live or a replay race. In case of a replay race
     * the timer is set to 
     */
    @Override
    public void raceTimesInfosReceived(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo,
            long clientTimeWhenRequestWasSent, Date serverTimeDuringRequest, long clientTimeWhenResponseWasReceived) {
        raceTimePanel.removeRaceTimesInfoProviderListener(this);
    }

    /**
     * Stops listening for leaderboard updates
     */
    @Override
    public void updatedLeaderboard(LeaderboardDTO leaderboard) {
        leaderboardPanel.removeLeaderboardUpdateListener(this);
    }

    @Override
    public void currentRaceSelected(RaceIdentifier raceIdentifier, RaceColumnDTO raceColumn) {
        // nothing to do
    }
    
    @Override
    public void competitorsForRaceDefined(Iterable<CompetitorDTO> competitorsInRace) {
        // nothing to do here
    }
}
