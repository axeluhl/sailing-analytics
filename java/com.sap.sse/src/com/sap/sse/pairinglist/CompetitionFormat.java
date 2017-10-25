package com.sap.sse.pairinglist;

import com.sap.sse.common.Util;

public interface CompetitionFormat<Flight, Group, Competitor> extends PairingFrameProvider {
    Iterable<Flight> getFlights();

    default int getFlightsCount() {
        return Util.size(getFlights());
    }

    Iterable<Group> getGroups(Flight flight);

    default int getGroupsCount(Flight flight) {
        return Util.size(getGroups(flight));
    }

    Iterable<Competitor> getCompetitors();

    default int getCompetitorsCount() {
        return Util.size(getCompetitors());
    }
}
