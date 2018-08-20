package com.sap.sailing.domain.abstractlog.race.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.abstractlog.race.CompetitorResult;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;

public class CompetitorResultsImpl extends ArrayList<CompetitorResult> implements CompetitorResults {
    private static final long serialVersionUID = 4928351242700897387L;

    @Override
    public boolean hasConflicts() {
        final Set<Integer> oneBasedRanks = new HashSet<>();
        for (final CompetitorResult r : this) {
            final int oneBasedRank = r.getOneBasedRank();
            if (oneBasedRank != 0) {
                if (!oneBasedRanks.add(oneBasedRank)) {
                    return true;
                }
            }
        }
        return false;
    }
}
