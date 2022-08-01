package com.sap.sailing.domain.markpassinghash.impl;

import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.markpassinghash.impl.TrackedRaceHashForMarkPassingComparatorImpl.TypeOfHash;
import com.sap.sailing.domain.tracking.impl.TrackedRaceImpl;

public class TrackedRaceHashForMarkPassingCalculationImpl {
    private final int competitor;
    private final int start;
    private final int end;
    private final int waypoints;
    private final int numberOfGPSFixes;
    private final int gpsFixes;

    public TrackedRaceHashForMarkPassingCalculationImpl(Map<TypeOfHash, Integer> hashValues) {
        this.competitor = hashValues.get(TypeOfHash.COMPETITOR);
        this.start = hashValues.get(TypeOfHash.START);
        this.end = hashValues.get(TypeOfHash.END);
        this.waypoints = hashValues.get(TypeOfHash.WAYPOINTS);
        this.numberOfGPSFixes = hashValues.get(TypeOfHash.NUMBEROFGPSFIXES);
        this.gpsFixes = hashValues.get(TypeOfHash.GPSFIXES);

    }

    public boolean equals(TrackedRaceImpl trackedRace) {
        TrackedRaceHashForMarkPassingComparatorImpl hashCalculator = new TrackedRaceHashForMarkPassingComparatorImpl(
                trackedRace);
        int competitorHash = 0;

        if (hashCalculator.calculateHashForWaypoints() != waypoints)
            return false;

        for (Competitor c : trackedRace.getRace().getCompetitors()) {
            competitorHash = competitorHash + hashCalculator.calculateHashForCompetitor(c);
        }

        if (competitorHash != competitor)
            return false;

        if (hashCalculator.calculateHashForStart() != start)
            return false;

        if (hashCalculator.calculateHashForEnd() != end)
            return false;

        if (hashCalculator.calculateHashForNumberOfGPSFixes() != numberOfGPSFixes)
            return false;

        if (hashCalculator.calculateHashForGPSFixes() != gpsFixes)
            return false;

        return true;
    }
}