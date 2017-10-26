package com.sap.see.pairinglist.test;

import com.sap.sse.pairinglist.PairingFrameProvider;

/**
 * This test class is used for reproducing a PairingFrameProvider
 * @author D070264
 *
 */

public class PairingFrameProviderTest implements PairingFrameProvider {

    private int flights, groups, competitors;
    
    public PairingFrameProviderTest(int flights, int groups, int competitors) {
        this.flights = flights;
        this.groups = groups;
        this.competitors = competitors;
    }
    
    @Override
    public int getFlightsCount() {
        return flights;
    }

    @Override
    public int getGroupsCount() {
        return groups;
    }

    @Override
    public int getCompetitorsCount() {
        return competitors;
    }

}
