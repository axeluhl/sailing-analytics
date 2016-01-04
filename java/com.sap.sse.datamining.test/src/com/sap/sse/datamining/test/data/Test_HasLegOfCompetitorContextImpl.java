package com.sap.sse.datamining.test.data;

import com.sap.sse.datamining.test.domain.Test_Competitor;
import com.sap.sse.datamining.test.domain.Test_Leg;

public class Test_HasLegOfCompetitorContextImpl implements Test_HasLegOfCompetitorContext {

    private Test_HasRaceContext raceContext;
    private Test_Leg leg;
    private int legNumber;
    private Test_Competitor competitor;

    public Test_HasLegOfCompetitorContextImpl(Test_HasRaceContext raceContext, Test_Leg leg, int legNumber, Test_Competitor competitor) {
        this.raceContext = raceContext;
        this.leg = leg;
        this.legNumber = legNumber;
        this.competitor = competitor;
    }
    
    @Override
    public Test_HasRaceContext getRaceContext() {
        return raceContext;
    }

    @Override
    public Test_Leg getLeg() {
        return leg;
    }

    @Override
    public int getLegNumber() {
        return legNumber;
    }

    @Override
    public Test_Competitor getCompetitor() {
        return competitor;
    }

}
