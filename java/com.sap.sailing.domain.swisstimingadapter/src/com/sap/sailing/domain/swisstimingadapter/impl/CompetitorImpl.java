package com.sap.sailing.domain.swisstimingadapter.impl;

import com.sap.sailing.domain.swisstimingadapter.Competitor;

public class CompetitorImpl implements Competitor {
    private final String boatID;
    private final String threeLetterIOCCode;
    private final String name;
    
    public CompetitorImpl(String boatID, String threeLetterIOCCode, String name) {
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
    
    @Override
    public String toString() {
        return name+"/"+boatID;
    }

}
