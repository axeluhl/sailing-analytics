package com.sap.sse.pairinglist;


public interface PairingList<Flight, Group, Competitor> {
    Iterable<Competitor> getCompetitors(Flight pFlight, Group pGroup);
    
    PairingFrameProvider<Object, Object, Object> getProvider();
}
