package com.sap.sailing.domain.test;

import java.io.Serializable;
import java.util.Collections;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorFactory;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sse.common.Color;

public abstract class AbstractLeaderboardTest {
    public static CompetitorImpl createCompetitor(String competitorName) {
        return new CompetitorImpl(competitorName, competitorName, Color.RED, null, null, new TeamImpl("STG", Collections.singleton(
                        new PersonImpl(competitorName, new NationalityImpl("GER"),
                        /* dateOfBirth */ null, "This is famous "+competitorName)),
                        new PersonImpl("Rigo van Maas", new NationalityImpl("NED"),
                        /* dateOfBirth */null, "This is Rigo, the coach")), new BoatImpl(competitorName + "'s boat",
                new BoatClassImpl("505", /* typicallyStartsUpwind */ true), /* sailID */ null), /* timeOnTimeFactor */ null, /* timeOnDistanceAllowancePerNauticalMile */ null, null);
    }

    public static Competitor createCompetitor(String competitorName, CompetitorFactory competitorFactory) {
        return competitorFactory.getOrCreateCompetitor(competitorName, competitorName, Color.RED, "someone@nobody.de", null, new TeamImpl("STG", Collections.singleton(
                new PersonImpl(competitorName, new NationalityImpl("GER"),
                /* dateOfBirth */ null, "This is famous "+competitorName)),
                new PersonImpl("Rigo van Maas", new NationalityImpl("NED"),
                /* dateOfBirth */null, "This is Rigo, the coach")), new BoatImpl(competitorName + "'s boat",
                new BoatClassImpl("505", /* typicallyStartsUpwind */ true), /* sailID */ null),
                /* timeOnTimeFactor */ null, /* timeOnDistanceAllowanceInSecondsPerNauticalMile */ null, null);
    }

    public static CompetitorImpl createCompetitor(String competitorName, Serializable id) {
        return new CompetitorImpl(id, competitorName, Color.RED, null, null, new TeamImpl("STG", Collections.singleton(
                        new PersonImpl(competitorName, new NationalityImpl("GER"),
                        /* dateOfBirth */ null, "This is famous "+competitorName)),
                        new PersonImpl("Rigo van Maas", new NationalityImpl("NED"),
                        /* dateOfBirth */null, "This is Rigo, the coach")), new BoatImpl(competitorName + "'s boat",
                new BoatClassImpl("505", /* typicallyStartsUpwind */ true), /* sailID */ null), /* timeOnTimeFactor */ null, /* timeOnDistanceAllowancePerNauticalMile */ null, null);
    }
}
