package com.sap.sailing.domain.racelog.analyzing.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.StartTimeFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.StartTimeFinderResult;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogDependentStartTimeEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogStartTimeEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.SimpleRaceLogIdentifierImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class DependentStartTimeFinderTest {

    private RaceLog raceLogA;
    private RaceLog raceLogB;
    private RaceLog raceLogC;

    private AbstractLogEventAuthor author;

    private TimePoint nowMock;
    
    private RaceLogResolver raceLogResolver;

    @Before
    public void setUp() {
        author = new LogEventAuthorImpl("Test", 1);
        
        raceLogA = new RaceLogImpl("raceLogA");
        raceLogB = new RaceLogImpl("raceLogB");
        raceLogC = new RaceLogImpl("raceLogC");

        nowMock = mock(TimePoint.class);

        raceLogResolver = new RaceLogResolver() {
            @Override
            public RaceLog resolve(SimpleRaceLogIdentifier identifier) {
                if (identifier.getRegattaLikeParentName().equals("A")) {
                    return raceLogA;
                } else if (identifier.getRegattaLikeParentName().equals("B")) {
                    return raceLogB;
                } else {
                    return raceLogC;
                }
            }
        };
    }

    @Test
    public void testDependentStartTimeUpdate() {
       raceLogC.add(new RaceLogDependentStartTimeEventImpl(nowMock, author, nowMock, "12", null, 12,
                new SimpleRaceLogIdentifierImpl("B", "", ""), new MillisecondsDurationImpl(5000)));

       StartTimeFinder finder = new StartTimeFinder(raceLogResolver, raceLogC);
       //Tests for correct behaviour, when race depending on has no starttime set
       assertNull(finder.analyze().getStartTime());
       
       raceLogB.add(new RaceLogDependentStartTimeEventImpl(nowMock, author, nowMock, "12", null, 12,
               new SimpleRaceLogIdentifierImpl("A", "", ""), new MillisecondsDurationImpl(5000)));
       
       finder = new StartTimeFinder(raceLogResolver, raceLogB);
       StartTimeFinderResult result = finder.analyze();
       
       List<SimpleRaceLogIdentifier> expectedDependingOnRaces = new ArrayList<SimpleRaceLogIdentifier>();
       expectedDependingOnRaces.add(new SimpleRaceLogIdentifierImpl("A", "", ""));
       assertNull(result.getStartTime());
       assertEquals(expectedDependingOnRaces, result.getRacesDependingOn());
       
       finder = new StartTimeFinder(raceLogResolver, raceLogC);
       //Tests for correct behaviour, when race depending on has a DependentStartTimeEvent and its parent start time is unknown
       expectedDependingOnRaces = new ArrayList<SimpleRaceLogIdentifier>();
       expectedDependingOnRaces.add(new SimpleRaceLogIdentifierImpl("B", "", ""));
       expectedDependingOnRaces.add(new SimpleRaceLogIdentifierImpl("A", "", ""));
       
       result = finder.analyze();
       assertNull(result.getStartTime());
       assertEquals(expectedDependingOnRaces, result.getRacesDependingOn());
       
       MillisecondsTimePoint now = MillisecondsTimePoint.now();
       raceLogA.add(new RaceLogStartTimeEventImpl(now, author, now, "12", null, 12, new MillisecondsTimePoint(5000)));
       
       finder = new StartTimeFinder(raceLogResolver, raceLogB);
       assertEquals(10000, finder.analyze().getStartTime().asMillis());
       finder = new StartTimeFinder(raceLogResolver, raceLogC);
       assertEquals(15000, finder.analyze().getStartTime().asMillis());
       
       //Test correct behaviour, when middle element changes
       raceLogB.add(new RaceLogStartTimeEventImpl(now, author, now, "12", null, 12, new MillisecondsTimePoint(15000)));
       finder = new StartTimeFinder(raceLogResolver, raceLogA);
       assertEquals(5000, finder.analyze().getStartTime().asMillis());
       finder = new StartTimeFinder(raceLogResolver, raceLogB);
       assertEquals(15000, finder.analyze().getStartTime().asMillis());
       finder = new StartTimeFinder(raceLogResolver, raceLogC);
       assertEquals(20000, finder.analyze().getStartTime().asMillis());
    }
    
    @Test
    public void testDependentStartTimeCycle() {
        raceLogC.add(new RaceLogDependentStartTimeEventImpl(nowMock, author, nowMock, "12", null, 12,
                new SimpleRaceLogIdentifierImpl("B", "", ""), new MillisecondsDurationImpl(5000)));
        
        raceLogB.add(new RaceLogDependentStartTimeEventImpl(nowMock, author, nowMock, "12", null, 12,
                new SimpleRaceLogIdentifierImpl("A", "", ""), new MillisecondsDurationImpl(5000)));
        
        raceLogA.add(new RaceLogDependentStartTimeEventImpl(nowMock, author, nowMock, "12", null, 12,
                new SimpleRaceLogIdentifierImpl("C", "", ""), new MillisecondsDurationImpl(5000)));

       
       //Check that all resolve to null in case of a cycle 
       StartTimeFinder finder = new StartTimeFinder(raceLogResolver, raceLogC);
       assertNull(finder.analyze().getStartTime());
       
       finder = new StartTimeFinder(raceLogResolver, raceLogB);
       assertNull(finder.analyze().getStartTime());
       
       finder = new StartTimeFinder(raceLogResolver, raceLogA);
       assertNull(finder.analyze().getStartTime());
       
       //Check that all resolve correctly after changing some element in cycle
       MillisecondsTimePoint now = MillisecondsTimePoint.now();
       raceLogB.add(new RaceLogStartTimeEventImpl(now, author, now, "12", null, 12,
               new MillisecondsTimePoint(5000)));
       
       //now A -> C -> B
       finder = new StartTimeFinder(raceLogResolver, raceLogA);
       //Tests for correct behaviour, when race depending on has a DependentStartTimeEvent and its parent start time is unknown
       List<SimpleRaceLogIdentifier> expectedDependingOnRaces = new ArrayList<SimpleRaceLogIdentifier>();
       expectedDependingOnRaces = new ArrayList<SimpleRaceLogIdentifier>();
       expectedDependingOnRaces.add(new SimpleRaceLogIdentifierImpl("C", "", ""));
       expectedDependingOnRaces.add(new SimpleRaceLogIdentifierImpl("B", "", ""));
       
       StartTimeFinderResult result = finder.analyze();
       assertEquals(15000, result.getStartTime().asMillis());
       assertEquals(expectedDependingOnRaces, result.getRacesDependingOn());
       
       //now C -> B
       finder = new StartTimeFinder(raceLogResolver, raceLogC);
       //Tests for correct behaviour, when race depending on has a DependentStartTimeEvent and its parent start time is unknown
       expectedDependingOnRaces = new ArrayList<SimpleRaceLogIdentifier>();
       expectedDependingOnRaces.add(new SimpleRaceLogIdentifierImpl("B", "", ""));
       
       result = finder.analyze();
       assertEquals(10000, result.getStartTime().asMillis());
       assertEquals(expectedDependingOnRaces, result.getRacesDependingOn());
       
       //now B
       finder = new StartTimeFinder(raceLogResolver, raceLogB);
       //Tests for correct behaviour, when race depending on has a DependentStartTimeEvent and its parent start time is unknown
       expectedDependingOnRaces = new ArrayList<SimpleRaceLogIdentifier>();
       
       result = finder.analyze();
       assertEquals(5000, result.getStartTime().asMillis());
       assertEquals(expectedDependingOnRaces, result.getRacesDependingOn());
    }
}
