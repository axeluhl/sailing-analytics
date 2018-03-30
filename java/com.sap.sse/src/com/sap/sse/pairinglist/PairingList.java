package com.sap.sse.pairinglist;

import com.sap.sse.common.Util.Pair;

/**
 * 
 * A ParingList is a concrete version of a PairingListTamplate, which has the ability to return a List which contains
 * specific Competitor objects for a specific flight. Because of this it has to depend on a
 * <code>CompetitonFormat</code>.
 */
public interface PairingList<Flight, Group, Competitor, CompetitorAllocation> {
    /**
     * Returns pairs of {@link Competitor}s and its {@link CompetitorAllocation} within a group of a specific flight.
     * 
     * @return {@link Pair}s of {@link Competitor}s and {@link CompetitorAllocation}
     */
    Iterable<Pair<Competitor, CompetitorAllocation>> getCompetitors(Flight flight, Group group);
}
