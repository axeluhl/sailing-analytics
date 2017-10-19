package com.sap.sse.pairinglist;

import java.util.Iterator;

public interface PairingList<Flight, Group, Competitor> {
    Iterator<Competitor> getCompetitors(Flight pFlight, Group pGroup);
    PairingFrameProvider<Object, Object, Object> getProvider();
}
