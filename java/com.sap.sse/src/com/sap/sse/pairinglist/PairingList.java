package com.sap.sse.pairinglist;

import com.sap.sse.common.Util.Pair;

/**
 * 
 * A ParingList is a concrete version of a PairingListTamplate, which has the ability to return a List
 * which contains specific Competitor objects for a specific flight. Because of this it has to depend on a
 * <code>CompetitonFormat</code>.
 */
public interface PairingList<Flight, Group, Competitor, CompetitorAllocation> {
    //TODO specify Javadoc for Boat-Competitor association
    /**
     * Returns the Competitor objects in a group of a specific flight
     * 
     * @return <code>Iterable</code> with competitors
     */
    Iterable<Pair<Competitor, CompetitorAllocation>> getCompetitors(Flight flight, Group group);
}
