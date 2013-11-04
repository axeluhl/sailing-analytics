package com.sap.sailing.domain.base;

import java.io.Serializable;

import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.domain.base.impl.DynamicTeam;

/**
 * Manages a set of {@link Competitor} objects. There may be a transient implementation based on a simple cache,
 * and there may be persistent implementations.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface CompetitorStore {

    Competitor getExistingCompetitorById(Serializable competitorId);
    
    Competitor getExistingCompetitorByIdAsString(String idAsString);

    Competitor getOrCreateCompetitor(Serializable competitorId, String name, DynamicTeam team, DynamicBoat boat);

    int size();

    /**
     * Removes all competitors from this store. Use with due care.
     */
    void clear();
    
    Iterable<? extends Competitor> getCompetitors();
    
    void removeCompetitor(Competitor competitor);

    /**
     * Updates the competitor with {@link Competitor#getId() ID} <code>id</code> by setting the name, sail ID and nationality to
     * the values provided. Doing so will not fire any events nor will it replicate this change from a master to any replicas.
     * The calling client has to make sure that the changes applied will reach replicas and all other interested clients. It will
     * be sufficient to ensure that subsequent DTOs produced from the competitor modified will reflect the changes.<p>
     * 
     * If no competitor with the ID requested is found, the call is a no-op, doing nothing, not even throwing an exception.
     * @return TODO
     */
    Competitor updateCompetitor(Serializable id, String newName, String newSailId, Nationality newNationality);
}
