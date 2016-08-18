package com.sap.sailing.domain.test;

import java.io.Serializable;
import java.util.Collections;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorFactory;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.CompetitorWithBoatImpl;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sse.common.Color;

public abstract class AbstractLeaderboardTest {
    private final static BoatClass boatClass = new BoatClassImpl("505", /* typicallyStartsUpwind */ true);
    
    public static CompetitorWithBoat createCompetitorAndBoat(String competitorName) {
        Competitor c = new CompetitorImpl(competitorName, competitorName, "KYC", Color.RED, null, null, new TeamImpl("STG", Collections.singleton(
                        new PersonImpl(competitorName, new NationalityImpl("GER"),
                        /* dateOfBirth */ null, "This is famous "+competitorName)),
                        new PersonImpl("Rigo van Maas", new NationalityImpl("NED"),
                        /* dateOfBirth */null, "This is Rigo, the coach")), new BoatImpl("id12345",
                competitorName + "'s boat", boatClass, /* sailID */ null), /* timeOnTimeFactor */ null, /* timeOnDistanceAllowancePerNauticalMile */ null, null);
        Boat b = new BoatImpl("id12345", competitorName + "'s boat", boatClass, /* sailID */ null);
        return new CompetitorWithBoatImpl(c, b);
    }

    public static CompetitorWithBoat createCompetitorAndBoat(String competitorName, CompetitorFactory competitorFactory) {
        Competitor c = competitorFactory.getOrCreateCompetitor(competitorName, competitorName, "WH", Color.RED, "someone@nobody.de", null, new TeamImpl("STG", Collections.singleton(
                new PersonImpl(competitorName, new NationalityImpl("GER"),
                /* dateOfBirth */ null, "This is famous "+competitorName)),
                new PersonImpl("Rigo van Maas", new NationalityImpl("NED"),
                /* dateOfBirth */null, "This is Rigo, the coach")), new BoatImpl("id12345",
                competitorName + "'s boat", boatClass, /* sailID */ null),
                /* timeOnTimeFactor */ null, /* timeOnDistanceAllowanceInSecondsPerNauticalMile */ null, null);
        Boat b = new BoatImpl("id12345", competitorName + "'s boat", boatClass, /* sailID */ null);
        return new CompetitorWithBoatImpl(c, b);
    }

    public static CompetitorWithBoat createCompetitorAndBoat(String competitorName, Serializable id) {
        Competitor c = new CompetitorImpl(id, competitorName, "KYC", Color.RED, null, null, new TeamImpl("STG", Collections.singleton(
                        new PersonImpl(competitorName, new NationalityImpl("GER"),
                        /* dateOfBirth */ null, "This is famous "+competitorName)),
                        new PersonImpl("Rigo van Maas", new NationalityImpl("NED"),
                        /* dateOfBirth */null, "This is Rigo, the coach")), new BoatImpl(id,
                competitorName + "'s boat", boatClass, /* sailID */ null), /* timeOnTimeFactor */ null, /* timeOnDistanceAllowancePerNauticalMile */ null, null);
        Boat b = new BoatImpl("id12345", competitorName + "'s boat", boatClass, /* sailID */ null);
        return new CompetitorWithBoatImpl(c, b);
    }
}
