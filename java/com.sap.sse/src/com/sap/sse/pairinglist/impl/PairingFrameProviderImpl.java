package com.sap.sse.pairinglist.impl;

import com.sap.sse.pairinglist.PairingFrameProvider;

public class PairingFrameProviderImpl<Flight, Group, Competitors> implements 
    PairingFrameProvider<Flight, Group, Competitors>{

    @SuppressWarnings("hiding")
    public<Flight> Flight[] getFlights() {
  
        return null;
    }

    @SuppressWarnings("hiding")
    public<Group> Group[] getGroups(Flight pFlight) {
        
        return null;
    }

    @Override
    public Iterable<Competitors> getCompetitors() {
        return null;
    }

    @Override
    public int getFlightsCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getGroupsCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getCompetitorCount() {
        // TODO Auto-generated method stub
        return 0;
    }
}
