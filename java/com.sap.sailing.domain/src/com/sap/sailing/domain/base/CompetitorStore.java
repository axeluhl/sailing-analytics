package com.sap.sailing.domain.base;

import java.io.Serializable;

/**
 * Manages a set of {@link Competitor} objects. There may be a transient implementation based on a simple cache,
 * and there may be persistent implementations.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface CompetitorStore {

    Competitor getExistingCompetitorById(Serializable competitorId);

    Competitor getOrCreateCompetitor(Serializable competitorId, String name, Team team, Boat boat);

    int size();

    /**
     * Removes all competitors from this store. Use with due care.
     */
    void clear();
}
