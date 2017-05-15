package com.sap.sailing.domain.swisstimingadapter.impl;

import java.util.List;

import com.sap.sailing.domain.swisstimingadapter.CrewMember;

public class CompetitorWithID extends AbstractCompetitor {
    private final String id;
    private List<CrewMember> crew;
    
    public CompetitorWithID(String id, String boatID, String threeLetterIOCCode, String name, List<CrewMember> crew) {
        super(boatID, threeLetterIOCCode, name);
        this.id = id;
        this.crew = crew;
    }

    public String getID() {
        return id;
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
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "CompetitorWithID [boatID=" + getBoatID() + ", threeLetterIOCCode=" + getThreeLetterIOCCode() + ", name=" + getName()
                + ", id=" + id + ", crew=" + getCrew() + "]";
    }
}
