package com.sap.sse.datamining.test.functions.registry.test_contexts;

import com.sap.sse.datamining.test.functions.registry.test_classes.Test_BoatClass;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Competitor;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Leg;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Race;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Regatta;

public class Test_HasLegOfCompetitorContextImpl extends Test_HasRaceContextImpl implements Test_HasLegOfCompetitorContext {

    private Test_Leg leg;
    private int legNumber;
    private Test_Competitor competitor;

    public Test_HasLegOfCompetitorContextImpl(Test_Regatta regatta, Test_Race race, Test_BoatClass boatClass, int year,
            Test_Leg leg, int legNumber, Test_Competitor competitor) {
        super(regatta, race, boatClass, year);
        this.leg = leg;
        this.legNumber = legNumber;
        this.competitor = competitor;
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
