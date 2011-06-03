package com.sap.sailing.domain.tractracadapter.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.tractrac.ResultAPI.LiveResult;
import com.tractrac.ResultAPI.LiveResultItem;
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
        refreshResultGenerator(timePoint);
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getRank(Competitor competitor, TimePoint timePoint) {
        refreshResultGenerator(timePoint);
        for (LiveResult liveResult : resultGenerator.getLiveResults()) {
            if (liveResult.getClassName().equals(getRace().getBoatClass().getName())) {
                for (LiveResultItem item : liveResult.values()) {
                    
                }
            }
        }
        return 0;
    }

}
