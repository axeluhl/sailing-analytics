package com.sap.sailing.simulator.impl;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.simulator.LegIdentifier;



public class LegIdentifierImpl extends RegattaNameAndRaceName implements LegIdentifier {
    private static final long serialVersionUID = 3599904513673776450L;
    private String legName;
        
    public LegIdentifierImpl(RegattaAndRaceIdentifier raceIdentifier, String legName) {
        super(raceIdentifier.getRegattaName(), raceIdentifier.getRaceName());
        this.legName = legName;
    }

    @Override
    public String getLegName() {
        return legName;
    }
    
    @Override
    public int getLegNumber() {
        return Integer.parseInt(legName);
    }
    
    @Override
    public String toString() {
        return getRegattaName()+"/"+getRaceName()+"/"+getLegName();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((legName == null) ? 0 : legName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        LegIdentifierImpl other = (LegIdentifierImpl) obj;
        if (legName == null) {
            if (other.legName != null)
                return false;
        } else if (!legName.equals(other.legName))
            return false;
        return true;
    }

}
