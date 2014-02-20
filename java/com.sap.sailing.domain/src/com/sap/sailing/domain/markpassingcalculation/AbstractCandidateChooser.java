package com.sap.sailing.domain.markpassingcalculation;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.tracking.MarkPassing;

public interface AbstractCandidateChooser {

    /**
     * Calculates any new {@link MarkPassing}s and notifies the {@link DynamicTrackedRace}.
     * 
     * @param candidateDeltas
     *            A pair of new {@link Candidate}s and those that should be removed.
     */

    public void calculateMarkPassDeltas(Competitor c, Pair<Iterable<Candidate>, Iterable<Candidate>> candidateDeltas);
}
