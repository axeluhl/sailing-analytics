package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.DegreePosition;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.tracking.impl.MarkPassingImpl;
import com.sap.sailing.util.Util;

public class WindEstimationOnStoredTracksTest extends StoredTrackedBasedTestWithTrackedRace {
    @Test
    public void testSuccessfulTrackedRaceCreation() throws FileNotFoundException, IOException {
        assertNotNull(getTrackedRace());
        assertEquals(Util.size(getTrackedRace().getRace().getCompetitors()), loadTracks().size());
    }

    @Test
    public void testSimpleWindEstimation() throws NoWindException {
        Competitor hasso = getCompetitorByName("Dr.Plattner");
        GPSFixTrack<Competitor, GPSFixMoving> hassosTrack = getTrackedRace().getTrack(hasso);
        TimePoint start = hassosTrack.getFirstRawFix().getTimePoint();
        TimePoint stop = hassosTrack.getLastRawFix().getTimePoint();
        TimePoint middle = new MillisecondsTimePoint(start.asMillis() + (stop.asMillis()-start.asMillis())*3/4);
        Wind estimatedWindDirection = getTrackedRace().getEstimatedWindDirection(/* position */ null, middle);
        assertNotNull(estimatedWindDirection);
    }
    
    @Test
    public void testWindEstimationForSimpleTracks() throws NoWindException {
        Competitor competitor1 = new CompetitorImpl(123, "Wolfgang Hunger", new TeamImpl("STG", Collections.singleton(
                new PersonImpl("Wolfgang Hunger", new NationalityImpl("Germany", "GER"),
                /* dateOfBirth */ null, "This is famous Wolfgang Hunger")), new PersonImpl("Rigo van Maas", new NationalityImpl("The Netherlands", "NED"),
                        /* dateOfBirth */ null, "This is Rigo, the coach")), new BoatImpl("Wolfgang Hunger's boat", new BoatClassImpl("505")));
        Competitor competitor2 = new CompetitorImpl(123, "Dr. Hasso Plattner", new TeamImpl("STG", Collections.singleton(
                new PersonImpl("Wolfgang Hunger", new NationalityImpl("Germany", "GER"),
                /* dateOfBirth */ null, "This is SAP co-founder Hasso Plattner")), new PersonImpl("Rigo van Maas", new NationalityImpl("The Netherlands", "NED"),
                        /* dateOfBirth */ null, "This is Rigo, the coach")), new BoatImpl("Wolfgang Hunger's boat", new BoatClassImpl("505")));
        setTrackedRace(createTestTrackedRace("Kieler Woche", "505 Race 2", "505",
                Arrays.asList(new Competitor[] { competitor1, competitor2 })));
        MillisecondsTimePoint now = MillisecondsTimePoint.now();
        Set<MarkPassing> markPassingForCompetitor1 = new HashSet<MarkPassing>();
        markPassingForCompetitor1.add(new MarkPassingImpl(now, getTrackedRace().getRace().getCourse()
                .getFirstWaypoint(), competitor1));
        getTrackedRace().updateMarkPassings(competitor1, markPassingForCompetitor1);
        Set<MarkPassing> markPassingForCompetitor2 = new HashSet<MarkPassing>();
        markPassingForCompetitor2.add(new MarkPassingImpl(now, getTrackedRace().getRace().getCourse()
                .getFirstWaypoint(), competitor2));
        getTrackedRace().updateMarkPassings(competitor2, markPassingForCompetitor2);
        DynamicTrack<Competitor, GPSFixMoving> hungersTrack = getTrackedRace().getTrack(competitor1);
        hungersTrack.addGPSFix(new GPSFixMovingImpl(new DegreePosition(54.4680424, 10.234451), now,
                new KnotSpeedWithBearingImpl(10, new DegreeBearingImpl(320))));
        DynamicTrack<Competitor, GPSFixMoving> hassosTrack = getTrackedRace().getTrack(competitor2);
        hassosTrack.addGPSFix(new GPSFixMovingImpl(new DegreePosition(54.4680424, 10.234451), now,
                new KnotSpeedWithBearingImpl(10, new DegreeBearingImpl(50))));
        Wind estimatedWindDirection = getTrackedRace().getEstimatedWindDirection(/* position */ null, now);
        assertEquals(5., estimatedWindDirection.getBearing().getDegrees(), 0.00000001);
    }

}
