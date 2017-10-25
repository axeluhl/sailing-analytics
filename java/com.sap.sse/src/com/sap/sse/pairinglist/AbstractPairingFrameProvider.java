package com.sap.sse.pairinglist;

public abstract class AbstractPairingFrameProvider implements PairingFrameProvider {
    @Override
    public boolean equals(Object other) {
        return other instanceof AbstractPairingFrameProvider &&
                this.getFlightsCount() == ((AbstractPairingFrameProvider) other).getFlightsCount() &&
                this.getGroupsCount() == ((AbstractPairingFrameProvider) other).getGroupsCount() &&
                this.getCompetitorsCount() == ((AbstractPairingFrameProvider) other).getCompetitorsCount();
    }
    
    @Override
    public int hashCode() {
        return 916439 ^ this.getFlightsCount() ^ this.getGroupsCount() ^ this.getCompetitorsCount();
    }
    
}
