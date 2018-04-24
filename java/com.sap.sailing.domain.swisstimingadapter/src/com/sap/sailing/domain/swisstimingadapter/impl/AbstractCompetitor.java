package com.sap.sailing.domain.swisstimingadapter.impl;

import com.sap.sailing.domain.swisstimingadapter.Competitor;

public abstract class AbstractCompetitor implements Competitor {
    private final String boatID;
    private final String threeLetterIOCCode;
    private final String name;

    public AbstractCompetitor(String boatID, String threeLetterIOCCode, String name) {
        super();
        this.boatID = boatID;
        this.threeLetterIOCCode = threeLetterIOCCode;
        this.name = name;
    }

    @Override
    public String getBoatID() {
        return boatID;
    }

    @Override
    public String getThreeLetterIOCCode() {
        return threeLetterIOCCode;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getIdAsString() {
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((boatID == null) ? 0 : boatID.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((threeLetterIOCCode == null) ? 0 : threeLetterIOCCode.hashCode());
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
        AbstractCompetitor other = (AbstractCompetitor) obj;
        if (boatID == null) {
            if (other.boatID != null)
                return false;
        } else if (!boatID.equals(other.boatID))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (threeLetterIOCCode == null) {
            if (other.threeLetterIOCCode != null)
                return false;
        } else if (!threeLetterIOCCode.equals(other.threeLetterIOCCode))
            return false;
        return true;
    }
}
