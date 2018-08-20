package com.sap.sailing.domain.common;

public class LegIdentifierImpl extends RegattaNameAndRaceName implements LegIdentifier {
    private static final long serialVersionUID = 3599904513673776450L;
    private RegattaAndRaceIdentifier raceIdentifier = null;
    private String legName;

    LegIdentifierImpl() {}
    
    public LegIdentifierImpl(RegattaAndRaceIdentifier raceIdentifier, String legName) {
        super(raceIdentifier.getRegattaName(), raceIdentifier.getRaceName());
        this.raceIdentifier = raceIdentifier;
        this.legName = legName;
    }

    @Override
    public RegattaAndRaceIdentifier getRaceIdentifier() {
        return this.raceIdentifier;
    }

    @Override
    public String getLegName() {
        return legName;
    }

    @Override
    public int getLegNumber() {
        return Integer.parseInt(legName)-1;
    }

    @Override
    public String toString() {
        return getRegattaName() + "/" + getRaceName() + "/" + getLegName();
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
