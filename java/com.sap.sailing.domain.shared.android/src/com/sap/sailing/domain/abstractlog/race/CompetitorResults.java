package com.sap.sailing.domain.abstractlog.race;


/**
 * A set of competitor results; order does not tell anything about the finishing order which can be seen from
 * the {@link CompetitorResult#getRank}.
 */
public interface CompetitorResults extends Iterable<CompetitorResult> {
    boolean add(CompetitorResult result);
}
