package com.sap.sse.pairinglist;


public interface PairingFrameProvider<Flight, Group, Competitor> {
    
        //Iterable<Flight> getFlights();
        int getFlightsCount();
        
        //Iterable<Group> getGroups(Flight flight);
        int getGroupsCount();
        
        Iterable<Competitor>  getCompetitors();
        int getCompetitorCount();
}
