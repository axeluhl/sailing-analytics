package com.sap.sse.pairinglist;

import com.sap.sse.common.Util;
/**
 * If a Class implements this Interface, it is possible to generate a ParingList out of this Class. 
 * The class needs some specific methods to get the attributes of the needed ParingList.
 */
public interface CompetitionFormat<Flight, Group, Competitor, CompetitorAllocation> extends PairingFrameProvider {
    Iterable<Flight> getFlights();

    default int getFlightsCount() {
        return Util.size(getFlights());
    }

    Iterable<Group> getGroups(Flight flight);

    Iterable<Competitor> getCompetitors();

    default int getCompetitorsCount() {
        return Util.size(getCompetitors());
    }
    
    Iterable<CompetitorAllocation> getCompetitorAllocation(); 
}
