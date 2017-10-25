package com.sap.sse.pairinglist;

public interface PairingList<Flight, Group, Competitor> {
    Iterable<Competitor> getCompetitors(Flight flight, Group group);
}
