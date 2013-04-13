package com.sap.sailing.server.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.analyzing.impl.FinishPositioningListFinder;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sailing.server.operationaltransformation.UpdateLeaderboardMaxPointsReason;
import com.sap.sailing.server.operationaltransformation.UpdateLeaderboardScoreCorrection;
import com.sap.sailing.server.operationaltransformation.UpdateLeaderboardScoreCorrectionMetadata;

public class RaceLogScoringReplicator implements RaceColumnListener {
    
    private static final long serialVersionUID = -5958519195756937338L;
    
    private final RacingEventService service;
    
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
    public void resultDiscardingRuleChanged(ThresholdBasedResultDiscardingRule oldDiscardingRule,
            ThresholdBasedResultDiscardingRule newDiscardingRule) {
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
            checkNeedForScoreCorrectionByResultsOfRaceCommittee(leaderboard, raceColumn, fleet, raceLog, event.getTimePoint());
        }
    }

    @Override
    public boolean isTransient() {
        return true;
    }
    
    /**
     * Retrieves the last RaceLogFinishPositioningListChangedEvent from the racelog and compares the ranks and disqualifications 
     * entered by the race committee with the tracked ranks. When a tracked rank for a competitor is not the same as the rank of the race committee,
     * a score correction is issued.
     * @param timePoint the TimePoint at which the race committee confirmed their last rank list entered in the app.
     */
    private void checkNeedForScoreCorrectionByResultsOfRaceCommittee(Leaderboard leaderboard, RaceColumn raceColumn, Fleet fleet, RaceLog raceLog, TimePoint timePoint) {
        
        int numberOfCompetitorsInLeaderboard = Util.size(leaderboard.getCompetitors());
        int numberOfCompetitorsInRace;
        boolean scoreHasBeenCorrected = false;
        
        numberOfCompetitorsInRace = getNumberOfCompetitorsInRace(raceColumn, fleet, numberOfCompetitorsInLeaderboard);
        
        FinishPositioningListFinder positioningListFinder = new FinishPositioningListFinder(raceLog);
        
        List<Triple<Serializable, String, MaxPointsReason>> positioningList = positioningListFinder.getFinishPositioningList();
        if (positioningList != null) {
            for (Triple<Serializable, String, MaxPointsReason> positionedCompetitor : positioningList) {
                Competitor competitor = DomainFactory.INSTANCE.getExistingCompetitorById(positionedCompetitor.getA());
                
                if (positionedCompetitor.getC().equals(MaxPointsReason.NONE)) {
                    try {
                        if (!leaderboard.getMaxPointsReason(competitor, raceColumn, timePoint).equals(MaxPointsReason.NONE)) {
                            applyMaxPointsReasonOperation(leaderboard, raceColumn, competitor, MaxPointsReason.NONE, timePoint);
                            scoreHasBeenCorrected = true;
                        }
                        
                        int rankByRaceCommittee = getRankInPositioningListByRaceCommittee(positioningList, positionedCompetitor);

                        Double scoreByRaceCommittee = leaderboard.getScoringScheme().getScoreForRank(raceColumn, competitor, rankByRaceCommittee, numberOfCompetitorsInRace);
                        Double trackedNetPoints = leaderboard.getNetPoints(competitor, raceColumn, timePoint);
                        if (trackedNetPoints == null || !trackedNetPoints.equals(scoreByRaceCommittee)) {
                            applyScoreCorrectionOperation(leaderboard, raceColumn, competitor, scoreByRaceCommittee, timePoint);
                            scoreHasBeenCorrected = true;
                        }
                    } catch (NoWindException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    MaxPointsReason trackedMaxPointsReason = leaderboard.getMaxPointsReason(competitor, raceColumn, timePoint);
                    MaxPointsReason maxPointsReasonByRaceCommittee = positionedCompetitor.getC();
                    if (!maxPointsReasonByRaceCommittee.equals(trackedMaxPointsReason)) {
                        applyMaxPointsReasonOperation(leaderboard, raceColumn, competitor, maxPointsReasonByRaceCommittee, timePoint);
                        scoreHasBeenCorrected = true;
                    }
                }
            }
            
            if (scoreHasBeenCorrected) {
                applyMetadataUpdate(leaderboard, timePoint, "Update triggered by Race Committee.");
            }
        }
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

    private int getRankInPositioningListByRaceCommittee(List<Triple<Serializable, String, MaxPointsReason>> positioningList, Triple<Serializable, String, MaxPointsReason> positionedCompetitor) {
        return positioningList.indexOf(positionedCompetitor) + 1;
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
