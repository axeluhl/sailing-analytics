package com.sap.sailing.domain.swisstimingadapter.impl;

import java.util.List;

import com.sap.sailing.domain.swisstimingadapter.CrewMember;

public class CompetitorWithID extends AbstractCompetitor {
    private final String idAsString;
    private List<CrewMember> crew;
    
    public CompetitorWithID(String idAsString, String boatID, String threeLetterIOCCode, String name, List<CrewMember> crew) {
        super(boatID, threeLetterIOCCode, name);
        this.idAsString = idAsString;
        this.crew = crew;
    }

    public String getIdAsString() {
        return idAsString;
    }

    @Override
    public List<CrewMember> getCrew() {
        return crew;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((crew == null) ? 0 : crew.hashCode());
        result = prime * result + ((idAsString == null) ? 0 : idAsString.hashCode());
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
        CompetitorWithID other = (CompetitorWithID) obj;
        if (crew == null) {
            if (other.crew != null)
                return false;
        } else if (!crew.equals(other.crew))
            return false;
        if (idAsString == null) {
            if (other.idAsString != null)
                return false;
        } else if (!idAsString.equals(other.idAsString))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "CompetitorWithID [boatID=" + getBoatID() + ", threeLetterIOCCode=" + getThreeLetterIOCCode() + ", name=" + getName()
                + ", id=" + idAsString + ", crew=" + getCrew() + "]";
    }
}
