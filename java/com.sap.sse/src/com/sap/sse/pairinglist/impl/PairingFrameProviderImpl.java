package com.sap.sse.pairinglist.impl;

import com.sap.sse.pairinglist.PairingFrameProvider;

public class PairingFrameProviderImpl<Flight, Group, Competitors> implements 
    PairingFrameProvider<Flight, Group, Competitors>{

    @SuppressWarnings("hiding")
    @Override
    public<Flight> Flight[] getFlights() {
  
        return null;
    }

    @SuppressWarnings("hiding")
    @Override
    public<Group> Group[] getGroups(Flight pFlight) {
        
        return null;
    }

    @Override
    public int getCompetitors() {
        return 0;
    }
}
