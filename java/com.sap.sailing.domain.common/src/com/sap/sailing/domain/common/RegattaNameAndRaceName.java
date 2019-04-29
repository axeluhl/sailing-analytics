package com.sap.sailing.domain.common;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;

public class RegattaNameAndRaceName extends RegattaName implements RegattaAndRaceIdentifier {
    private static final long serialVersionUID = 3599904513673776450L;
    private String raceName;
    
    RegattaNameAndRaceName() {}
    
    public RegattaNameAndRaceName(String regattaName, String raceName) {
        super(regattaName);
        this.raceName = raceName;
    }
    public String getRaceName() {
        return raceName;
    }
    
    @Override
    public Object getRace(RaceFetcher raceFetcher) {
        return raceFetcher.getRace(this);
    }
    @Override
    public Object getTrackedRace(RaceFetcher raceFetcher) {
        return raceFetcher.getTrackedRace(this);
    }
    @Override
    public Object getExistingTrackedRace(RaceFetcher raceFetcher) {
        return raceFetcher.getExistingTrackedRace(this);
    }
    @Override
    public String toString() {
        return getRegattaName()+"/"+getRaceName();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((raceName == null) ? 0 : raceName.hashCode());
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
        RegattaNameAndRaceName other = (RegattaNameAndRaceName) obj;
        if (raceName == null) {
            if (other.raceName != null)
                return false;
        } else if (!raceName.equals(other.raceName))
            return false;
        return true;
    }

    @Override
    public QualifiedObjectIdentifier getIdentifier() {
        return getType().getQualifiedObjectIdentifier(getTypeRelativeObjectIdentifier());
    }

    @Override
    public TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier() {
        return getTypeRelativeObjectIdentifier(getRegattaName(), getRaceName());
    }

    public static TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier(String regattaName, String raceName) {
        return new TypeRelativeObjectIdentifier(regattaName, raceName);
    }

    @Override
    public HasPermissions getType() {
        return SecuredDomainType.TRACKED_RACE;
    }

    @Override
    public String getName() {
        return toString();
    }

}
