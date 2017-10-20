package com.sap.sse.pairinglist;

public interface PairingFrameProvider<Flight, Group, Competitors> {
    
        @SuppressWarnings("hiding")
        public<Flight> Flight[] getFlights();
        
        @SuppressWarnings("hiding")
        public<Group> Group[] getGroups(Flight pFlight);
        
        int  getCompetitors();
}
