package com.sap.sse.pairinglist;

public interface PairingFrameProvider<Flight, Group, Competitor> {
    
        Iterable<Flight> getFlights();
        int getFlightsCount();
        
        Iterable<Group> getGroups(Flight flight);
        int getGroupsCount();
        
        Iterable<Competitor>  getCompetitors();
        Iterable<Competitor>  getCompetitors(Flight pFlight, Group pGroup);
        int getCompetitorCount();
}
