package com.sap.sailing.domain.swisstimingadapter.impl;

import java.util.List;

import com.sap.sailing.domain.swisstimingadapter.Competitor;
import com.sap.sailing.domain.swisstimingadapter.CrewMember;

public class CompetitorWithID implements Competitor {
    private final String boatID;
    private final String threeLetterIOCCode;
    private final String name;
    private final String id;

    private List<CrewMember> crew;
    
    public CompetitorWithID(String id, String boatID, String threeLetterIOCCode, String name, List<CrewMember> crew) {
        super();
        this.boatID = boatID;
        this.threeLetterIOCCode = threeLetterIOCCode;
        this.name = name;
        this.id = id;
        this.crew = crew;
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
    
    public String getID() {
        return id;
    }

    @Override
    public String toString() {
        return "CompetitorWithID [boatID=" + boatID + ", threeLetterIOCCode=" + threeLetterIOCCode + ", name=" + name
                + ", id=" + id + "]";
    }

    @Override
    public List<CrewMember> getCrew() {
        return crew;
    }

}
