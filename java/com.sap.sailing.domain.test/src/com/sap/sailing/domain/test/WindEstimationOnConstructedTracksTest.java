package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
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
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.tracking.impl.MarkPassingImpl;

public class WindEstimationOnConstructedTracksTest extends StoredTrackBasedTest {
    private Competitor competitor1;
    private Competitor competitor2;
    
    @Before
    public void setUp() {
        competitor1 = new CompetitorImpl(123, "Wolfgang Hunger", new TeamImpl("STG", Collections.singleton(
                new PersonImpl("Wolfgang Hunger", new NationalityImpl("Germany", "GER"),
                /* dateOfBirth */ null, "This is famous Wolfgang Hunger")), new PersonImpl("Rigo van Maas", new NationalityImpl("The Netherlands", "NED"),
                        /* dateOfBirth */ null, "This is Rigo, the coach")), new BoatImpl("Wolfgang Hunger's boat", new BoatClassImpl("505")));
        competitor2 = new CompetitorImpl(123, "Dr. Hasso Plattner", new TeamImpl("STG", Collections.singleton(
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
    }

    @Test
    public void testWindEstimationForSimpleTracks() throws NoWindException {
        MillisecondsTimePoint now = MillisecondsTimePoint.now();
        setBearingForCompetitor(competitor1, now, 320);
        setBearingForCompetitor(competitor2, now, 50);
        Wind estimatedWindDirection = getTrackedRace().getEstimatedWindDirection(/* position */ null, now);
        assertEquals(5., estimatedWindDirection.getBearing().getDegrees(), 0.00000001);
    }

    private void setBearingForCompetitor(Competitor competitor, MillisecondsTimePoint timePoint, double bearingDeg) {
        DynamicTrack<Competitor, GPSFixMoving> hungersTrack = getTrackedRace().getTrack(competitor);
        hungersTrack.addGPSFix(new GPSFixMovingImpl(new DegreePosition(54.4680424, 10.234451), timePoint,
                new KnotSpeedWithBearingImpl(10, new DegreeBearingImpl(bearingDeg))));
    }

}
