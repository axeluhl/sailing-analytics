package com.sap.sailing.polars.caching;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.polars.aggregation.PolarFixRaceInterval;
import com.sap.sailing.util.SmartFutureCache.UpdateInterval;
import com.sap.sse.common.Util.Pair;

public class PolarFixCacheRaceInterval implements UpdateInterval<PolarFixCacheRaceInterval>, PolarFixRaceInterval {

    private final Map<TrackedRace, Map<Competitor, Pair<TimePoint, TimePoint>>> competitorAndTimepointsForRace;

    public PolarFixCacheRaceInterval(
            Map<TrackedRace, Map<Competitor, Pair<TimePoint, TimePoint>>> competitorAndTimepointsForRace) {
        this.competitorAndTimepointsForRace = competitorAndTimepointsForRace;
    }

    @Override
    public Map<TrackedRace, Map<Competitor, Pair<TimePoint, TimePoint>>> getCompetitorAndTimepointsForRace() {
        return competitorAndTimepointsForRace;
    }

    @Override
    public PolarFixCacheRaceInterval join(PolarFixCacheRaceInterval otherUpdateInterval) {
        Map<TrackedRace, Map<Competitor, Pair<TimePoint, TimePoint>>> joinedInterval = new HashMap<TrackedRace, Map<Competitor, Pair<TimePoint, TimePoint>>>();
        Set<TrackedRace> racesContainedInEitherMap = new HashSet<TrackedRace>();
        for (TrackedRace race : competitorAndTimepointsForRace.keySet()) {
            racesContainedInEitherMap.add(race);
        }
        Map<TrackedRace, Map<Competitor, Pair<TimePoint, TimePoint>>> otherUpdateIntervalInExplicitForm = otherUpdateInterval
                .getCompetitorAndTimepointsForRace();
        for (TrackedRace race : otherUpdateIntervalInExplicitForm.keySet()) {
            racesContainedInEitherMap.add(race);
        }

        for (TrackedRace race : racesContainedInEitherMap) {
            HashMap<Competitor, Pair<TimePoint, TimePoint>> joinedMap = new HashMap<Competitor, Pair<TimePoint, TimePoint>>();
            Map<Competitor, Pair<TimePoint, TimePoint>> mapOfTimpointCompetitorPairs = competitorAndTimepointsForRace
                    .get(race);
            if (mapOfTimpointCompetitorPairs != null) {
                joinedMap.putAll(mapOfTimpointCompetitorPairs);
            }
            Map<Competitor, Pair<TimePoint, TimePoint>> mapOfOtherTimpointCompetitorPairs = otherUpdateIntervalInExplicitForm
                    .get(race);
            if (mapOfOtherTimpointCompetitorPairs != null) {
                mergeMap(joinedMap, mapOfOtherTimpointCompetitorPairs);
            }
            joinedInterval.put(race, joinedMap);
        }
        return new PolarFixCacheRaceInterval(joinedInterval);
    }

    private void mergeMap(HashMap<Competitor, Pair<TimePoint, TimePoint>> joinedMap,
            Map<Competitor, Pair<TimePoint, TimePoint>> mapOfOtherTimpointCompetitorPairs) {
        for (Entry<Competitor, Pair<TimePoint, TimePoint>> otherEntry : mapOfOtherTimpointCompetitorPairs.entrySet()) {

            Competitor competitor = otherEntry.getKey();
            Pair<TimePoint, TimePoint> otherValue = otherEntry.getValue();
            Pair<TimePoint, TimePoint> existingValue = joinedMap.get(competitor);
            if (existingValue != null) {
                Pair<TimePoint, TimePoint> joinedTimepointInterval = joinTimepointIntervals(existingValue, otherValue);
                joinedMap.put(competitor, joinedTimepointInterval);
            } else {
                joinedMap.put(competitor, otherValue);
            }

        }
    }

    /**
     * This method ignores a possible gap between the two intervals.
     * 
     * @param existingValue
     * @param otherValue
     * @return
     */
    private Pair<TimePoint, TimePoint> joinTimepointIntervals(Pair<TimePoint, TimePoint> existingValue,
            Pair<TimePoint, TimePoint> otherValue) {
        TimePoint startExisting = existingValue.getA();
        TimePoint endExisting = existingValue.getB();
        TimePoint startOther = otherValue.getA();
        TimePoint endOther = otherValue.getB();

        TimePoint startNew = startExisting.before(startOther) ? startExisting : startOther;
        TimePoint endNew = endExisting.after(endOther) ? endExisting : endOther;

        return new Pair<TimePoint, TimePoint>(startNew, endNew);
    }

}