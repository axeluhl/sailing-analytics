package com.sap.sse.pairinglist;

public interface PairingFrameProvider<Flight, Group, Competitors> {
    
        Flight[] getFlights();
        
        Group[] getGroups(Flight pFlight);
        
        int  getCompetitors();
}
