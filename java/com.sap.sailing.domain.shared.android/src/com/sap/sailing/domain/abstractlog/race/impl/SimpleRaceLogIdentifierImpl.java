package com.sap.sailing.domain.abstractlog.race.impl;

import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;

public class SimpleRaceLogIdentifierImpl implements SimpleRaceLogIdentifier {
    private static final long serialVersionUID = 5377117723950808853L;
    protected final String regattaLikeParentName;
    protected final String raceColumnName;
    protected final String fleetName;
    
    public SimpleRaceLogIdentifierImpl(String regattaLikeParentName, String raceColumnName, String fleetName) {
        this.regattaLikeParentName = regattaLikeParentName;
        this.raceColumnName = raceColumnName;
        this.fleetName = fleetName;
    }

    @Override
    public String getRegattaLikeParentName() {
        return regattaLikeParentName;
    }

    @Override
    public String getRaceColumnName() {
        return raceColumnName;
    }

    @Override
    public String getFleetName() {
        return fleetName;
    }

    @Override
    public String toString() {
        return "SimpleRaceLogIdentifier [regattaLikeParentName=" + regattaLikeParentName + ", raceColumnName=" + raceColumnName
                + ", fleetName=" + fleetName + "]";
    }

    @Override
    public com.sap.sse.common.Util.Triple<String, String, String> getIdentifier() {
        return new com.sap.sse.common.Util.Triple<String, String, String>(
                regattaLikeParentName, raceColumnName, fleetName);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fleetName == null) ? 0 : fleetName.hashCode());
        result = prime * result + ((raceColumnName == null) ? 0 : raceColumnName.hashCode());
        result = prime * result + ((regattaLikeParentName == null) ? 0 : regattaLikeParentName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SimpleRaceLogIdentifierImpl other = (SimpleRaceLogIdentifierImpl) obj;
        if (fleetName == null) {
            if (other.fleetName != null)
                return false;
        } else if (!fleetName.equals(other.fleetName))
            return false;
        if (raceColumnName == null) {
            if (other.raceColumnName != null)
                return false;
        } else if (!raceColumnName.equals(other.raceColumnName))
            return false;
        if (regattaLikeParentName == null) {
            if (other.regattaLikeParentName != null)
                return false;
        } else if (!regattaLikeParentName.equals(other.regattaLikeParentName))
            return false;
        return true;
    }
}
