package com.sap.sailing.server.impl;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Callable;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.ConfirmedFinishPositioningListFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.FinishPositioningListFinder;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.ResultDiscardingRule;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sailing.server.operationaltransformation.UpdateLeaderboardMaxPointsReason;
import com.sap.sailing.server.operationaltransformation.UpdateLeaderboardScoreCorrection;
import com.sap.sailing.server.operationaltransformation.UpdateLeaderboardScoreCorrectionMetadata;
import com.sap.sse.common.Util;

public class RaceLogScoringReplicator implements RaceColumnListener {
    
    private static final long serialVersionUID = -5958519195756937338L;
    
    private final RacingEventService service;
    private final static String COMMENT_TEXT_ON_SCORE_CORRECTION = "Update triggered by Race Committee.";
    
    public RaceLogScoringReplicator(RacingEventService service) {
        this.service = service;
    }

    @Override
    public void trackedRaceLinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
    }

    @Override
    public void trackedRaceUnlinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
    }

    @Override
    public void isMedalRaceChanged(RaceColumn raceColumn, boolean newIsMedalRace) {
    }

    @Override
    public void isStartsWithZeroScoreChanged(RaceColumn raceColumn, boolean newIsStartsWithZeroScore) {
    }

    @Override
    public void hasSplitFleetContiguousScoringChanged(RaceColumn raceColumn, boolean hasSplitFleetContiguousScoring) {
    }

    @Override
    public void isFirstColumnIsNonDiscardableCarryForwardChanged(RaceColumn raceColumn, boolean firstColumnIsNonDiscardableCarryForward) {
    }

    @Override
    public boolean canAddRaceColumnToContainer(RaceColumn raceColumn) {
        return true;
    }

    @Override
    public void raceColumnAddedToContainer(RaceColumn raceColumn) {
    }

    @Override
    public void raceColumnRemovedFromContainer(RaceColumn raceColumn) {
    }

    @Override
    public void raceColumnMoved(RaceColumn raceColumn, int newIndex) {
    }

    @Override
    public void factorChanged(RaceColumn raceColumn, Double oldFactor, Double newFactor) {
    }

    @Override
    public void competitorDisplayNameChanged(Competitor competitor, String oldDisplayName, String displayName) {
    }

    @Override
    public void resultDiscardingRuleChanged(ResultDiscardingRule oldDiscardingRule, ResultDiscardingRule newDiscardingRule) {
    }

    @Override
    public void raceLogEventAdded(RaceColumn raceColumn, RaceLogIdentifier raceLogIdentifier, RaceLogEvent event) {
        if (event instanceof RaceLogFinishPositioningConfirmedEvent) {
            handleFinishPositioningList(raceColumn, raceLogIdentifier, event);
        }
    }

    private void handleFinishPositioningList(RaceColumn raceColumn, RaceLogIdentifier raceLogIdentifier, RaceLogEvent event) {
        Leaderboard leaderboard = service.getLeaderboardByName(raceLogIdentifier.getTemplate().getParentObjectName());
        if (leaderboard != null) {
            Fleet fleet = raceColumn.getFleetByName(raceLogIdentifier.getFleetName());
            RaceLog raceLog = raceColumn.getRaceLog(fleet);
            checkNeedForScoreCorrectionByResultsOfRaceCommittee(leaderboard, raceColumn, fleet, raceLog, event.getCreatedAt());
        }
    }

    @Override
    public boolean isTransient() {
        return true;
    }
    
    /**
     * Retrieves the last RaceLogFinishPositioningListChangedEvent from the racelog and compares the ranks and
     * disqualifications entered by the race committee with the tracked ranks. When a tracked rank for a competitor is
     * not the same as the rank of the race committee, a score correction is issued. The positioning list contains a
     * list of competitors sorted by the positioning order when finishing. Additionally a MaxPointsReason might be
     * entered by the Race Committee.
     * 
     * @param timePoint
     *            the TimePoint at which the race committee confirmed their last rank list entered in the app.
     */
    private void checkNeedForScoreCorrectionByResultsOfRaceCommittee(Leaderboard leaderboard, RaceColumn raceColumn, Fleet fleet, RaceLog raceLog, TimePoint timePoint) {
        
        int numberOfCompetitorsInLeaderboard = Util.size(leaderboard.getCompetitors());
        int numberOfCompetitorsInRace;
        List<com.sap.sse.common.Util.Triple<Serializable, String, MaxPointsReason>> positioningList;
        
        numberOfCompetitorsInRace = getNumberOfCompetitorsInRace(raceColumn, fleet, numberOfCompetitorsInLeaderboard);
        
        ConfirmedFinishPositioningListFinder confirmedPositioningListFinder = new ConfirmedFinishPositioningListFinder(raceLog);
        positioningList = confirmedPositioningListFinder.analyze();
        
        if (positioningList == null) {
            // we expect this case for old sailing events such as ESS Singapore, Quingdao, where the confirmation event did not contain the finish
            // positioning list
            FinishPositioningListFinder positioningListFinder = new FinishPositioningListFinder(raceLog);
            positioningList = positioningListFinder.analyze();
        }
        
        if (positioningList != null) {
            for (com.sap.sse.common.Util.Triple<Serializable, String, MaxPointsReason> positionedCompetitor : positioningList) {
                Competitor competitor = service.getBaseDomainFactory().getExistingCompetitorById(positionedCompetitor.getA());

                if (positionedCompetitor.getC().equals(MaxPointsReason.NONE)) {
                    try {
                        resetMaxPointsReasonIfNecessary(leaderboard, raceColumn, timePoint, competitor);
                        int rankByRaceCommittee = getRankInPositioningListByRaceCommittee(positioningList, positionedCompetitor);
                        correctScoreInLeaderboard(leaderboard, raceColumn, timePoint, numberOfCompetitorsInRace, 
                                competitor, rankByRaceCommittee);
                    } catch (NoWindException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    setMaxPointsReasonInLeaderboardIfNecessary(leaderboard, raceColumn, timePoint, positionedCompetitor, competitor);
                }
            }

            //Since the metadata update is used by the Sailing suite to determine the final state of a race, it has to be triggered, even though 
            //no score correction was performed
            applyMetadataUpdate(leaderboard, timePoint, COMMENT_TEXT_ON_SCORE_CORRECTION);
        }
    }

    private boolean setMaxPointsReasonInLeaderboardIfNecessary(Leaderboard leaderboard, RaceColumn raceColumn,
            TimePoint timePoint, com.sap.sse.common.Util.Triple<Serializable, String, MaxPointsReason> positionedCompetitor, Competitor competitor) {
        boolean scoreHasBeenCorrected = false;
        
        MaxPointsReason trackedMaxPointsReason = leaderboard.getMaxPointsReason(competitor, raceColumn, timePoint);
        MaxPointsReason maxPointsReasonByRaceCommittee = positionedCompetitor.getC();
        if (!maxPointsReasonByRaceCommittee.equals(trackedMaxPointsReason)) {
            applyMaxPointsReasonOperation(leaderboard, raceColumn, competitor, maxPointsReasonByRaceCommittee, timePoint);
            scoreHasBeenCorrected = true;
        }
        return scoreHasBeenCorrected;
    }

    private boolean correctScoreInLeaderboard(Leaderboard leaderboard, RaceColumn raceColumn, TimePoint timePoint,
            final int numberOfCompetitorsInRace, 
            Competitor competitor, int rankByRaceCommittee) throws NoWindException {
        boolean scoreHasBeenCorrected = false;
        Double scoreByRaceCommittee = leaderboard.getScoringScheme().getScoreForRank(raceColumn, competitor, rankByRaceCommittee,
                new Callable<Integer>() {
                    @Override
                    public Integer call() {
                        return numberOfCompetitorsInRace;
                    }
                }, leaderboard.getNumberOfCompetitorsInLeaderboardFetcher());
        
        // Do ALWAYS apply score corrections from race committee
        applyScoreCorrectionOperation(leaderboard, raceColumn, competitor, scoreByRaceCommittee, timePoint);
        scoreHasBeenCorrected = true;
        return scoreHasBeenCorrected;
    }

    private boolean resetMaxPointsReasonIfNecessary(Leaderboard leaderboard, RaceColumn raceColumn, TimePoint timePoint, Competitor competitor) {
        boolean scoreHasBeenCorrected = false;
        if (!leaderboard.getMaxPointsReason(competitor, raceColumn, timePoint).equals(MaxPointsReason.NONE)) {
            applyMaxPointsReasonOperation(leaderboard, raceColumn, competitor, MaxPointsReason.NONE, timePoint);
            scoreHasBeenCorrected = true;
        }
        return scoreHasBeenCorrected;
    }
    
    private void applyScoreCorrectionOperation(Leaderboard leaderboard, RaceColumn raceColumn, Competitor competitor, Double correctedScore, TimePoint timePoint) {
        RacingEventServiceOperation<?> operation = new UpdateLeaderboardScoreCorrection(leaderboard.getName(), raceColumn.getName(), competitor.getId().toString(), correctedScore, timePoint);
        service.apply(operation);
    }
    
    private void applyMaxPointsReasonOperation(Leaderboard leaderboard, RaceColumn raceColumn, Competitor competitor, MaxPointsReason reason, TimePoint timePoint) {
        RacingEventServiceOperation<?> operation = new UpdateLeaderboardMaxPointsReason(leaderboard.getName(), raceColumn.getName(), competitor.getId().toString(), reason, timePoint);
        service.apply(operation);
    }
    
    private void applyMetadataUpdate(Leaderboard leaderboard, TimePoint timePointOfLastCorrectionValidity, String comment) {
        RacingEventServiceOperation<?> operation = new UpdateLeaderboardScoreCorrectionMetadata(leaderboard.getName(), timePointOfLastCorrectionValidity, comment);
        service.apply(operation);
    }

    /**
     * The positioning list contains a list of competitors sorted by the positioning order when finishing. Additionally a MaxPointsReason might be entered by the 
     * Race Committee. The rank of a competitor according to the Race Committee is represented by the position in the given positioningList
     * @param positioningList The list containing the competitors. The rank is represented by the position of a competitor in the list
     * @param positionedCompetitor the competitor whose rank shall be determined
     * @return the rank of the given positionedCompetitor
     */
    private int getRankInPositioningListByRaceCommittee(List<com.sap.sse.common.Util.Triple<Serializable, String, MaxPointsReason>> positioningList, com.sap.sse.common.Util.Triple<Serializable, String, MaxPointsReason> positionedCompetitor) {
        return positioningList.indexOf(positionedCompetitor) + 1; // indexOf gives the zero-based position requested competitor in the list, + 1 gives the one-based rank
    }

    private int getNumberOfCompetitorsInRace(RaceColumn raceColumn, Fleet fleet, int numberOfCompetitorsInLeaderboard) {
        int numberOfCompetitorsInRace;
        if (raceColumn.getRaceDefinition(fleet) != null) {
            numberOfCompetitorsInRace = Util.size(raceColumn.getRaceDefinition(fleet).getCompetitors());
        } else {
            numberOfCompetitorsInRace = numberOfCompetitorsInLeaderboard;
        }
        return numberOfCompetitorsInRace;
    }

}
