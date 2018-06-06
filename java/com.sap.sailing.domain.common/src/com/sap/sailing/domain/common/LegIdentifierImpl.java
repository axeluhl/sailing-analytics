package com.sap.sailing.domain.common;

public class LegIdentifierImpl extends RegattaNameAndRaceName implements LegIdentifier {
    private static final long serialVersionUID = 3599904513673776450L;
    private RegattaAndRaceIdentifier raceIdentifier = null;
    private int oneBasedLegIndex;

    LegIdentifierImpl() {}
    
    public LegIdentifierImpl(RegattaAndRaceIdentifier raceIdentifier, int oneBasedLegIndex) {
        super(raceIdentifier.getRegattaName(), raceIdentifier.getRaceName());
        this.raceIdentifier = raceIdentifier;
        this.oneBasedLegIndex = oneBasedLegIndex;
    }

    @Override
    public RegattaAndRaceIdentifier getRaceIdentifier() {
        return this.raceIdentifier;
    }

    @Override
    public int getOneBasedLegIndex() {
        return oneBasedLegIndex;
    }

    @Override
    public String toString() {
        return getRegattaName() + "/" + getRaceName() + "/ leg " + getOneBasedLegIndex();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((raceIdentifier == null) ? 0 : raceIdentifier.hashCode());
        result = prime * result + oneBasedLegIndex;
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
        if (raceIdentifier == null) {
            if (other.raceIdentifier != null)
                return false;
        } else if (!raceIdentifier.equals(other.raceIdentifier))
            return false;
        if (oneBasedLegIndex != other.oneBasedLegIndex)
            return false;
        return true;
    }
}
