package com.sap.see.pairinglist.test;

import com.sap.sse.pairinglist.AbstractPairingFrameProvider;

/**
 * Instances of this class are used for test generations of PairingLists
 *
 */
public class PairingFrameProviderTest extends AbstractPairingFrameProvider {

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
