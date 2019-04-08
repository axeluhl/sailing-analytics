package com.sap.sailing.domain.swisstimingadapter.impl;

import java.util.List;

import com.sap.sailing.domain.swisstimingadapter.CrewMember;

public class CompetitorWithoutID extends AbstractCompetitor {
    public CompetitorWithoutID(String boatID, String threeLetterIOCCode, String name) {
        super(boatID, threeLetterIOCCode, name);
    }

    @Override
    public String getIdAsString() {
        return null;
    }

    @Override
    public List<CrewMember> getCrew() {
        return null;
    }

    @Override
    public String toString() {
        return "CompetitorWithoutID [boatID=" + getBoatID() + ", threeLetterIOCCode=" + getThreeLetterIOCCode() + ", name=" + getName() + "]";
    }
}
