package com.sap.sailing.domain.racelog.state.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventFactory;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogDependentStartTimeEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogStartTimeEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.SimpleRaceLogIdentifierImpl;
import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.domain.abstractlog.race.state.RaceStateChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.BaseRaceStateChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.impl.RaceStateImpl;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.RacingProcedureFactoryImpl;
import com.sap.sailing.domain.base.configuration.ConfigurationLoader;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.base.configuration.impl.EmptyRegattaConfiguration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class DependentRaceStateTest {

    private RaceLog raceLogA;
    private RaceLog raceLogB;
    private RaceLog raceLogC;

    private RaceState stateA;
    private RaceState stateB;
    private RaceState stateC;

    private RaceStateChangedListener listenerA;
    private RaceStateChangedListener listenerB;
    private RaceStateChangedListener listenerC;

    private AbstractLogEventAuthor author;
    private RaceLogEventFactory factory;
    private ConfigurationLoader<RegattaConfiguration> configuration;

    private TimePoint nowMock;
    
    private RaceLogResolver raceLogResolver;

    @Before
    public void setUp() {
        author = new LogEventAuthorImpl("Test", 1);
        factory = RaceLogEventFactory.INSTANCE;
        configuration = new EmptyRegattaConfiguration();

        raceLogA = new RaceLogImpl("raceLogA");
        raceLogB = new RaceLogImpl("raceLogB");
        raceLogC = new RaceLogImpl("raceLogC");

        listenerA = mock(RaceStateChangedListener.class);
        listenerB = mock(RaceStateChangedListener.class);
        listenerC = mock(RaceStateChangedListener.class);

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

        stateA = new RaceStateImpl(raceLogResolver, raceLogA, author, factory, new RacingProcedureFactoryImpl(author,
                factory, configuration));
        stateB = new RaceStateImpl(raceLogResolver, raceLogB, author, factory, new RacingProcedureFactoryImpl(author,
                factory, configuration));
        stateC = new RaceStateImpl(raceLogResolver, raceLogC, author, factory, new RacingProcedureFactoryImpl(author,
                factory, configuration));

        stateA.addChangedListener(listenerA);
        stateB.addChangedListener(listenerB);
        stateC.addChangedListener(listenerC);
    }

    @Test
    public void testCorrectAmountOfStartTimeChanges() {
       raceLogC.add(new RaceLogDependentStartTimeEventImpl(nowMock, author, nowMock, "12", null, 12,
                new SimpleRaceLogIdentifierImpl("B", "", ""), new MillisecondsDurationImpl(5000)));

       raceLogB.add(new RaceLogDependentStartTimeEventImpl(nowMock, author, nowMock, "12", null, 12,
               new SimpleRaceLogIdentifierImpl("A", "", ""), new MillisecondsDurationImpl(5000)));
       
       TimePoint now = MillisecondsTimePoint.now();
       raceLogA.add(new RaceLogStartTimeEventImpl(now, author, now, "12", null, 12, new MillisecondsTimePoint(5000)));
       
       verify(listenerC, times(3)).onStartTimeChanged(stateC);
       verify(listenerB, times(2)).onStartTimeChanged(stateB);
       verify(listenerA, times(1)).onStartTimeChanged(stateA);
    }

    @Test
    public void testInitialRegistrationOfRaceState() {
        final MillisecondsDurationImpl delta = new MillisecondsDurationImpl(5000);
        raceLogB.add(new RaceLogDependentStartTimeEventImpl(nowMock, author, nowMock, "12", null, 12,
                new SimpleRaceLogIdentifierImpl("A", "", ""), delta));
        final RaceState stateB2 = new RaceStateImpl(raceLogResolver, raceLogB, author, factory,
                new RacingProcedureFactoryImpl(author, factory, configuration));
        final TimePoint[] bTime = new TimePoint[1];
        stateB2.addChangedListener(new BaseRaceStateChangedListener() {
            @Override
            public void onStartTimeChanged(ReadonlyRaceState state) {
                bTime[0] = state.getStartTime();
            }
        });
        TimePoint now = MillisecondsTimePoint.now();
        raceLogA.add(new RaceLogStartTimeEventImpl(now, author, now, "12", null, 12, now));
        assertEquals(now.plus(delta), bTime[0]);
    }    

    @Test
    public void testCorrectAmountOfStartTimeChanges2() {
       raceLogC.add(new RaceLogDependentStartTimeEventImpl(nowMock, author, nowMock, "12", null, 12,
                new SimpleRaceLogIdentifierImpl("B", "", ""), new MillisecondsDurationImpl(5000)));

       raceLogB.add(new RaceLogDependentStartTimeEventImpl(nowMock, author, nowMock, "12", null, 12,
               new SimpleRaceLogIdentifierImpl("A", "", ""), new MillisecondsDurationImpl(5000)));
       
       TimePoint now = MillisecondsTimePoint.now();
       raceLogA.add(new RaceLogStartTimeEventImpl(now, author, now, "12", null, 12, new MillisecondsTimePoint(5000)));
       
       raceLogB.add(new RaceLogStartTimeEventImpl(now, author, now, "12", null, 12, new MillisecondsTimePoint(20000)));
       
       verify(listenerA, times(1)).onStartTimeChanged(stateA);
       verify(listenerB, times(3)).onStartTimeChanged(stateB);
       verify(listenerC, times(4)).onStartTimeChanged(stateC);
    }
}
