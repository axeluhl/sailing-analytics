package com.sap.sailing.domain.test;

import java.util.Collections;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.CompetitorWithBoatImpl;
import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.domain.base.impl.DynamicCompetitorWithBoat;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sse.common.Color;

public abstract class AbstractLeaderboardTest {
    private final static BoatClass boatClass = new BoatClassImpl("505", /* typicallyStartsUpwind */ true);
    
    public static Competitor createCompetitor(String competitorName) {
        return new CompetitorImpl(competitorName, competitorName, "KYC", Color.RED, null, null, new TeamImpl("STG", Collections.singleton(
                        new PersonImpl(competitorName, new NationalityImpl("GER"),
                        /* dateOfBirth */ null, "This is famous "+competitorName)),
                        new PersonImpl("Rigo van Maas", new NationalityImpl("NED"),
                        /* dateOfBirth */null, "This is Rigo, the coach")),
                        /* timeOnTimeFactor */ null, /* timeOnDistanceAllowancePerNauticalMile */ null, /* searchTag */ null);
    }

    public static DynamicBoat createBoat(String competitorName) {
        return new BoatImpl("id12345", competitorName + "'s boat", boatClass, /* sailID */ null);
    }

    public static DynamicCompetitorWithBoat createCompetitorWithBoat(String competitorName) {
        DynamicBoat b = (DynamicBoat) createBoat(competitorName);
        return new CompetitorWithBoatImpl(competitorName, competitorName, "KYC", Color.RED, null, null, new TeamImpl("STG", Collections.singleton(
                        new PersonImpl(competitorName, new NationalityImpl("GER"),
                        /* dateOfBirth */ null, "This is famous "+competitorName)),
                        new PersonImpl("Rigo van Maas", new NationalityImpl("NED"),
                        /* dateOfBirth */null, "This is Rigo, the coach")),
                        /* timeOnTimeFactor */ null, /* timeOnDistanceAllowancePerNauticalMile */ null, /* searchTag */ null, b);
    }
}
