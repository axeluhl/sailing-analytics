package com.sap.sse.pairinglist;

import com.sap.sse.common.Util;

/**
 * This interface is used to get the objects that are required for a {@link PairingList}. 
 */
public interface CompetitionFormat<Flight, Group, Competitor, CompetitorAllocation> extends PairingFrameProvider {
    Iterable<Flight> getFlights();

    default int getFlightsCount() {
        return Util.size(getFlights());
    }

    Iterable<? extends Group> getGroups(Flight flight);

    Iterable<Competitor> getCompetitors();

    default int getCompetitorsCount() {
        return Util.size(getCompetitors());
    }

    Iterable<CompetitorAllocation> getCompetitorAllocation();

    default int getMaxNumberOfCompetitorAllocationsNeeded() {
        final int div = getCompetitorsCount() / getGroupsCount();
        final int mod = getCompetitorsCount() % getGroupsCount();
        return div + (mod > 0 ? 1 : 0); // round up
    }
}
