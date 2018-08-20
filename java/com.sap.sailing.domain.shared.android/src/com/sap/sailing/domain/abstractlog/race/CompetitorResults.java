package com.sap.sailing.domain.abstractlog.race;


/**
 * A set of competitor results; order does not tell anything about the finishing order which can be seen from
 * the {@link CompetitorResult#getRank}.
 */
public interface CompetitorResults extends Iterable<CompetitorResult> {
    boolean add(CompetitorResult result);
    void clear();
    int size();

    /**
     * @return {@code true} if there are more than one {@link CompetitorResult} contained with equal
     * {@link CompetitorResult#getOneBasedRank() rank}. A client can take this as an indicator that
     * there may be an inconsistency, caused, e.g., by merging results entered on different devices
     * concurrently. The client shall use the {@link #iterator()} method to enumerate the
     * {@link CompetitorResult} objects, finding the conflicting elements.
     */
    boolean hasConflicts();
}
