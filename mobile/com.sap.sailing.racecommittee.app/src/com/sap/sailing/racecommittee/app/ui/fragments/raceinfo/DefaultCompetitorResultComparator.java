package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import com.sap.sailing.domain.abstractlog.race.CompetitorResult;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.racecommittee.app.ui.comparators.CompetitorSailIdComparator;
import com.sap.sailing.racecommittee.app.ui.comparators.CompetitorShortNameComparator;
import com.sap.sse.common.util.NaturalComparator;

import java.util.Comparator;

/**
 * Implements the default order for competitor results for the finishing list. Competitors with a non-{@code null} or
 * non-{@link MaxPointsReason#NONE} penalty are sorted to the end ("greater"), and within this group by their score, if
 * any. All other entries are sorted by their {@link CompetitorResult#getOneBasedRank() rank}, except for those with
 * rank 0, meaning "no rank." Those are considered greater than all other un-penalized entries with non-zero ranks.
 *
 * @author Axel Uhl (D043530)
 */
public class DefaultCompetitorResultComparator implements Comparator<CompetitorResult> {
    /**
     * Tells whether a lower score is better than a higher score.
     */
    private final boolean lowPoint;
    private final NaturalComparator naturalComparator;

    public DefaultCompetitorResultComparator(boolean lowPoint) {
        this.lowPoint = lowPoint;
        this.naturalComparator = new NaturalComparator();
    }

    @Override
    public int compare(CompetitorResult lhs, CompetitorResult rhs) {
        int result;
        // compare by rank
        final int lRank = lhs.getOneBasedRank() == 0 ? Integer.MAX_VALUE : lhs.getOneBasedRank();
        final int rRank = rhs.getOneBasedRank() == 0 ? Integer.MAX_VALUE : rhs.getOneBasedRank();
        if (lRank != rRank) {
            result = lRank - rRank;
        } else {
            MaxPointsReason lMPR = lhs.getMaxPointsReason();
            MaxPointsReason rMPR = rhs.getMaxPointsReason();
            if (lMPR == null || lMPR == MaxPointsReason.NONE) {
                if (rMPR == null || rMPR == MaxPointsReason.NONE) {
                    result = 0;
                } else {
                    result = -1; // lhs is "less" than rhs because we want to sort penalized competitors towards the end
                    // ("greater")
                }
            } else {
                if (rMPR == null || rMPR == MaxPointsReason.NONE) {
                    result = 1; // lhs is "greater" than lhs because we want to sort penalized competitors towards the
                    // end ("greater")
                } else {
                    // both are penalized; compare by score:
                    final int preResult = lhs.getScore() == null ? rhs.getScore() == null ? 0 : -1
                            : rhs.getScore() == null ? 1 : lhs.getScore().compareTo(rhs.getScore());
                    result = lowPoint ? preResult : -preResult;
                }
                //Compare further properties
                if (result == 0) {
                    //Sail ID
                    result = CompetitorSailIdComparator.compare(lhs.getBoatSailId(), rhs.getBoatSailId(), naturalComparator);
                }
                if (result == 0) {
                    //Short name
                    result = CompetitorShortNameComparator.compare(lhs.getShortName(), rhs.getShortName(), naturalComparator);
                }
                if (result == 0) {
                    //Competitor name
                    result = naturalComparator.compare(lhs.getName(), rhs.getName());
                }
            }
        }
        return result;
    }
}
