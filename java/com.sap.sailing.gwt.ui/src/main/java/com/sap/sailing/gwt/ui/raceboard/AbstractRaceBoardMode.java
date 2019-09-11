package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.settings.client.raceboard.RaceBoardPerspectiveOwnSettings;
import com.sap.sailing.gwt.ui.actions.GetLeaderboardByNameAction;
import com.sap.sailing.gwt.ui.actions.GetLeaderboardForRaceAction;
import com.sap.sailing.gwt.ui.client.LeaderboardUpdateListener;
import com.sap.sailing.gwt.ui.client.RaceTimePanel;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProviderListener;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceCompetitorSet.CompetitorsForRaceDefinedListener;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.SingleRaceLeaderboardPanel;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;

/**
 * Abstract base class for implementing a {@link RaceBoardMode}. The {@link #applyTo(RaceBoardPanel)} method registers
 * this object as a leaderboard update listener and as a race times info provider listener, also as a
 * {@link CompetitorsForRaceDefinedListener} in order to learn when the set of competitors for the race displayed by the
 * player is known. The default implementations of the corresponding callback methods will simply unregister this object
 * as a listener for the respective callback, enters the knowledge gained in the object's fields and calls the
 * {@link #trigger} method each time (abstract in this class). Subclasses can and should do the following:
 * 
 * <ul>
 * <li>Redefine {@link #applyTo(RaceBoardPanel)} and call the {@code stopReceiving...} methods for those types of
 *     events they don't need</li>
 * <li>Redefine specific callback methods in case more processing is required or canceling the listener is desired only
 * when certain preconditions are met</li>
 * <li>Implement the {@link #trigger} method to check if all information required has been received by now and then use
 * it to configure the {@link RaceBoard} and its components accordingly</li>
 * </ul>
 * 
 * Note that the {@link #updatedLeaderboard(LeaderboardDTO)} method will typically be called several times in more or
 * less unpredictable order, asynchronously. Calling {@link Timer#setTime(long)} will trigger
 * {@link #updatedLeaderboard(LeaderboardDTO)} eventually, but in between, other callbacks to the same method
 * may occur, triggered by other, automatic adjustments or initialization of the {@link Timer}. To solve this
 * problem, subclasses may decide to use a separate {@link GetLeaderboardByNameAction} action and execute
 * it using the {@link #getLeaderboardPanel()}.{@link ClassicLeaderboardPanel#getExecutor() getExecutor()}. In the
 * dedicated callback the subclass can then be sure that the {@link LeaderboardDTO} received matches the
 * time previously set on the {@link Timer}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public abstract class AbstractRaceBoardMode implements RaceBoardMode, RaceTimesInfoProviderListener, LeaderboardUpdateListener {
    private Timer timer;
    private RegattaAndRaceIdentifier raceIdentifier;
    private RaceTimePanel raceTimePanel;
    private SingleRaceLeaderboardPanel leaderboardPanel;
    private RaceBoardPanel raceBoardPanel;
    
    private Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo;
    private RaceTimesInfoDTO raceTimesInfoForRace;
    private LeaderboardDTO leaderboard;
    private LeaderboardDTO leaderboardForSpecificTimePoint;
    private RaceColumnDTO raceColumn;
    private final Collection<Runnable> runnablesToRunAfterInitializationFinished = new LinkedList<>();
    
    @Override
    public void applyTo(RaceBoardPanel raceBoardPanel) {
        this.raceBoardPanel = raceBoardPanel;
        this.raceTimePanel = raceBoardPanel.getRaceTimePanel();
        this.raceTimePanel.addRaceTimesInfoProviderListener(this);
        this.leaderboardPanel = raceBoardPanel.getLeaderboardPanel();
        this.leaderboardPanel.addLeaderboardUpdateListener(this);
        leaderboardPanel.setAutoExpandPreSelected(true);
        this.timer = raceBoardPanel.getTimer();
        this.raceIdentifier = raceBoardPanel.getSelectedRaceIdentifier();
        raceBoardPanel.getMap().addMapInitializedListener(new Runnable() {
            public void run() {
                checkIfTrigger();
            };
        });
    }
    
    protected void checkIfTrigger() {
        if (raceBoardPanel.getMap().isDataInitialized()) {
            trigger();
        }
    }

    @Override
    public void addInitializationFinishedRunner(Runnable runnable) {
        runnablesToRunAfterInitializationFinished.add(runnable);
    }

    /** called after initialization and trigger method is finished */
    protected void onInitializationFinished() {
        runnablesToRunAfterInitializationFinished.forEach(r -> r.run());
    }

    /**
     * Called whenever new information of any sort has become available. Subclasses can use this
     * to decide whether everything they need has been received in order to carry out their actions.
     */
    protected abstract void trigger();
    
    protected Timer getTimer() {
        return timer;
    }

    protected RegattaAndRaceIdentifier getRaceIdentifier() {
        return raceIdentifier;
    }

    protected RaceTimePanel getRaceTimePanel() {
        return raceTimePanel;
    }

    protected SingleRaceLeaderboardPanel getLeaderboardPanel() {
        return leaderboardPanel;
    }

    protected RaceBoardPanel getRaceBoardPanel() {
        return raceBoardPanel;
    }

    /**
     * Called after the {@link RaceTimePanel} has reacted to this update. We assume that now the timing for the race has
     * been received, and it should be clear by now whether we're talking about a live or a replay race. The
     * {@link #raceTimesInfo} field is set to the {@code raceTimesInfo} object received as parameter, the
     * {@link #raceTimesInfoForRace} field is set to the race times info specific to the race identified by
     * {@link #getRaceIdentifier()}.
     */
    @Override
    public void raceTimesInfosReceived(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo,
            long clientTimeWhenRequestWasSent, Date serverTimeDuringRequest, long clientTimeWhenResponseWasReceived) {
        this.raceTimesInfo = raceTimesInfo;
        this.raceTimesInfoForRace = raceTimesInfo.get(getRaceIdentifier());
        checkIfTrigger();
    }

    protected void stopReceivingRaceTimesInfos() {
        raceTimePanel.removeRaceTimesInfoProviderListener(this);
    }

    /**
     * Stops listening for leaderboard updates
     */
    @Override
    public void updatedLeaderboard(LeaderboardDTO leaderboard) {
        this.leaderboard = leaderboard;
        checkIfTrigger();
        ;
    }

    protected void stopReceivingLeaderboard() {
        leaderboardPanel.removeLeaderboardUpdateListener(this);
    }

    /**
     * Fetches the leaderboard named {@code leaderboardName} for {@code timePoint} with details for race column
     * {@code raceColumnName} and stores it in {@link #leaderboardForSpecificTimePoint}, then invokes
     * {@link #checkIfTrigger()}. If a {@link #getLeaderboard()} has been received already, it is passed on in the call
     * in order to reduce the bandwidth required, based on a differential approach.
     */
    protected void loadLeaderboardForSpecificTimePoint(String leaderboardName,
            String raceColumnName, Date timePoint) {
        final AsyncCallback<LeaderboardDTO> callback = new AsyncCallback<LeaderboardDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Error trying to load leaderboard", caught);
            }

            @Override
            public void onSuccess(LeaderboardDTO result) {
                leaderboardForSpecificTimePoint = result;
                getLeaderboardPanel().updateLeaderboard(result);
                checkIfTrigger();
            }
        };
        final ArrayList<String> raceColumnNameAsList = new ArrayList<>();
        if (getRaceColumn() != null) {
            raceColumnNameAsList.add(getRaceColumn().getName());
        }
        final GetLeaderboardForRaceAction getLeaderboardByNameAction = new GetLeaderboardForRaceAction(
                getLeaderboardPanel().getSailingService(), getLeaderboardPanel().getLeaderboard().getName(),
                raceIdentifier, getTimer().getTime(), raceColumnNameAsList, /* addOverallDetails */ false,
                getLeaderboard(), /* fillTotalPointsUncorrected */ false,
                /* timerToAdjustOffsetIn */ getTimer(), /* errorReporter */ null, StringMessages.INSTANCE);
        getLeaderboardPanel().getExecutor().execute(getLeaderboardByNameAction, LeaderboardPanel.LOAD_LEADERBOARD_DATA_CATEGORY, callback);
    }

    @Override
    public void currentRaceSelected(RaceIdentifier raceIdentifier, RaceColumnDTO raceColumn) {
        this.raceColumn = raceColumn;
        checkIfTrigger();
    }
    
    protected Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> getRaceTimesInfo() {
        return raceTimesInfo;
    }
    
    protected RaceTimesInfoDTO getRaceTimesInfoForRace() {
        return raceTimesInfoForRace;
    }

    protected LeaderboardDTO getLeaderboard() {
        return leaderboard;
    }

    protected LeaderboardDTO getLeaderboardForSpecificTimePoint() {
        return leaderboardForSpecificTimePoint;
    }

    protected RaceColumnDTO getRaceColumn() {
        return raceColumn;
    }
    
    protected void setTimerOrUseCustomStart(final TimePoint startPlayingAt) {
        final PlayModes playMode = getTimer().getPlayMode();
        TimePoint startPlayingAtOverride = null;
        if (playMode != PlayModes.Live && getRaceTimesInfoForRace() != null) {
            final RaceBoardPerspectiveOwnSettings perspectiveOwnSettings = raceBoardPanel.getSettings().getPerspectiveOwnSettings();
            if (perspectiveOwnSettings != null) {
                final Duration initialDurationAfterRaceStartInReplay = perspectiveOwnSettings.getInitialDurationAfterRaceStartInReplay();
                if (initialDurationAfterRaceStartInReplay != null) {
                    final Date relativeTo;
                    if (getRaceTimesInfoForRace().getStartOfRace() != null) {
                        relativeTo = getRaceTimesInfoForRace().getStartOfRace();
                    } else {
                        relativeTo = getRaceTimesInfoForRace().getStartOfTracking();
                    }
                    startPlayingAtOverride = new MillisecondsTimePoint(
                            relativeTo.getTime() + initialDurationAfterRaceStartInReplay.asMillis());
                }
            }
        }
        getTimer().setTime(
                startPlayingAtOverride != null ? startPlayingAtOverride.asMillis() : startPlayingAt.asMillis());
    }
}
