package com.sap.sailing.domain.tractracadapter.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.tractrac.ResultAPI.LiveResult;
import com.tractrac.ResultAPI.LiveResultItem;
import com.tractrac.ResultAPI.MarkResult;
import com.tractrac.ResultAPI.MarkResultItem;
import com.tractrac.ResultAPI.PerLegResult;
import com.tractrac.ResultAPI.ResultGenerator;

public class TracTracTrackedRaceImpl extends DynamicTrackedRaceImpl implements DynamicTrackedRace {

    /**
     * Note, that a result generator may cover multiple regattas in multiple classes. When results are
     * obtained from it, the appropriate boat class / event and race need to be selected.
     */
    private final ResultGenerator resultGenerator;
    
    private TimePoint resultGeneratorValidFor;

    public TracTracTrackedRaceImpl(ResultGenerator resultGenerator, TrackedEvent trackedEvent, RaceDefinition race,
            long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed) {
        super(trackedEvent, race, millisecondsOverWhichToAverageWind, millisecondsOverWhichToAverageSpeed);
        this.resultGenerator = resultGenerator;
    }
    
    /**
     * If needed because the results for a time point different from {@link #resultGeneratorValidFor} are requested,
     * recalculates the results provided by the {@link #resultGenerator}. In order to get a wind estimate, as position
     * the position of the start of the race course is used.
     */
    private void refreshResultGenerator(TimePoint timePoint) {
        if (!timePoint.equals(resultGeneratorValidFor)) {
            resultGeneratorValidFor = timePoint;
            Position p = getTrack(getRace().getCourse().getFirstWaypoint().getBuoys().iterator().next())
                    .getEstimatedPosition(timePoint);
            resultGenerator.recalcuateWithWind(timePoint.asMillis(), getWind(p, timePoint).getBearing().getDegrees());
        }
    }

    @Override
    protected TrackedLeg createTrackedLeg(RaceDefinition race, Leg leg) {
        return new TracTracTrackedLegImpl(this, leg, race.getCompetitors());
    }

    @Override
    public int getRankDifference(Competitor competitor, Leg leg, TimePoint timePoint) {
        MarkResultItem markResult = getCompetitorMarkResults(competitor, leg.getFrom(), timePoint);
        if (markResult != null) {
            return markResult.getDeltarank();
        } else {
            throw new RuntimeException("Didn't find mark results for competitor "+competitor+" in leg "+leg+" at time point "+timePoint);
        }
    }

    @Override
    public int getRank(Competitor competitor, TimePoint timePoint) {
        LiveResultItem competitorItem = getCompetitorLiveResults(competitor, timePoint);
        if (competitorItem != null) {
            return competitorItem.getRank();
        } else {
            throw new RuntimeException("Couldn't find live results for competitor "+competitor+" at time "+timePoint);
        }
    }

    /**
     * {@link #refreshResultGenerator(TimePoint) Updates the Trac Trac result generator} to <code>timePoint</code>
     * and fetches the live results for <code>competitor</code> for this race's boat class. If no such live data
     * is found, <code>null</code> is returned.
     */
    protected LiveResultItem getCompetitorLiveResults(Competitor competitor, TimePoint timePoint) {
        refreshResultGenerator(timePoint);
        LiveResultItem competitorItem = null;
        for (LiveResult liveResult : resultGenerator.getLiveResults()) {
            if (liveResult.getClassName().equals(getRace().getBoatClass().getName())) {
                for (LiveResultItem item : liveResult.values()) {
                    if (item.getUuid().equals(competitor.getId())) {
                        // found result record for right competitor
                        competitorItem = item;
                        break;
                    }
                }
            }
        }
        return competitorItem;
    }

    /**
     * {@link #refreshResultGenerator(TimePoint) Updates the Trac Trac result generator} to <code>timePoint</code> and
     * fetches the leg results for <code>competitor</code> in the leg starting at <code>legStartWaypoint</code> for this
     * race's boat class. If no such mark data is found, <code>null</code> is returned.
     */
    protected MarkResultItem getCompetitorMarkResults(Competitor competitor, Waypoint legStartWaypoint, TimePoint timePoint) {
        refreshResultGenerator(timePoint);
        int waypointIndex = getRace().getCourse().getIndexOfWaypoint(legStartWaypoint);
        MarkResultItem competitorItem = null;
        for (PerLegResult perLegResult : resultGenerator.getPerLegResults()) {
            if (perLegResult.getClassName().equals(getRace().getBoatClass().getName())) {
                MarkResult markResult = perLegResult.getMarkResult(waypointIndex);
                for (MarkResultItem item : markResult.values()) {
                    if (item.getUuid().equals(competitor.getId())) {
                        // found result record for right competitor
                        competitorItem = item;
                        break;
                    }
                }
            }
        }
        return competitorItem;
    }

}
