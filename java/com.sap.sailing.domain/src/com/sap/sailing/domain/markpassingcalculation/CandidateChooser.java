package com.sap.sailing.domain.markpassingcalculation;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.markpassingcalculation.impl.CandidateImpl;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.MarkPassing;

public interface CandidateChooser {
import com.sap.sse.common.Util;

    /**
     * Calculates any new {@link MarkPassing}s and notifies the {@link DynamicTrackedRace}.
     * 
     * @param candidateDeltas
     *            new {@link CandidateImpl}s and those that should be removed.
     */

    public void calculateMarkPassDeltas(Competitor c, Iterable<Candidate> newCans, Iterable<Candidate> oldCans);

    void removeWaypoints(Iterable<Waypoint> ways);

    void setFixedPassing(Competitor c, Waypoint w, TimePoint t);

    void removeFixedPassing(Competitor c, Waypoint w);
    
    void suppressMarkPassings(Competitor c, Waypoint firstSuppressedWaypoint);
    
    void stopSuppressingMarkPassings(Competitor c);
    
}
