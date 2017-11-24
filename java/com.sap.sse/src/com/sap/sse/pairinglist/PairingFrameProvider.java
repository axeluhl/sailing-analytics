package com.sap.sse.pairinglist;

public interface PairingFrameProvider {
    int getFlightsCount();
    int getGroupsCount();
    int getCompetitorsCount();
    
    default int getHashCode() {
        return 916439 ^ this.getFlightsCount() ^ this.getGroupsCount() ^ this.getCompetitorsCount();
    }
}
