package com.sap.sailing.domain.swisstimingadapter.impl;

import java.util.List;

import com.sap.sailing.domain.swisstimingadapter.Competitor;
import com.sap.sailing.domain.swisstimingadapter.CrewMember;

public class CompetitorWithoutID implements Competitor {
    private final String boatID;
    private final String threeLetterIOCCode;
    private final String name;
    
    public CompetitorWithoutID(String boatID, String threeLetterIOCCode, String name) {
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
    
    public String getID() {
        return null;
    }

	@Override
	public String toString() {
		return "CompetitorWithoutID [boatID=" + boatID + ", threeLetterIOCCode="
				+ threeLetterIOCCode + ", name=" + name + "]";
	}

	@Override
	public List<CrewMember> getCrew() {
		return null;
	}

}
