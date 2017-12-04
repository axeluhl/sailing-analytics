package com.sap.sse.pairinglist;

public interface PairingFrameProvider {
    int getFlightsCount();
    int getGroupsCount();
    int getCompetitorsCount();
    
    default int getHashCode() {
        int groupsCount = this.getGroupsCount();
        String result = "916" + this.getFlightsCount() + this.getGroupsCount() + this.getCompetitorsCount();
        return Integer.parseInt(result);
    }
}
