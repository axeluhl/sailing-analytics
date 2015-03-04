package com.sap.sailing.domain.racelog.analyzing.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.MultiLogAnalyzer;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.impl.RaceLogDeviceCompetitorMappingEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.impl.RaceLogRegisterCompetitorEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceCompetitorMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogRegisterCompetitorEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.impl.RegattaLogImpl;
import com.sap.sailing.domain.abstractlog.shared.analyzing.DeviceMappingFinder;
import com.sap.sailing.domain.abstractlog.shared.analyzing.RegisteredCompetitorsAnalyzer;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sailing.domain.racelogtracking.impl.SmartphoneUUIDIdentifierImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.WithID;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class MultiLogAnalyzerTest {
    private static TimePoint now() {
        return MillisecondsTimePoint.now();
    }

    private static TimePoint t(long millis) {
        return new MillisecondsTimePoint(millis);
    }

    private static UUID uuid() {
        return UUID.randomUUID();
    }

    private RaceLog raceLog;
    private RegattaLog regattaLog;
    private AbstractLogEventAuthor author;

    private static Competitor createCompetitor(int n) {
        return new CompetitorImpl(n, n + "", null, null, null, null);
    }

     private static DeviceIdentifier createDevice() {
     return new SmartphoneUUIDIdentifierImpl(UUID.randomUUID());
     }

    @Before
    public void setup() {
        raceLog = new RaceLogImpl(0);
        regattaLog = new RegattaLogImpl(0);
        author = new LogEventAuthorImpl("test", 0);
    }

    @Test
    public void registerCompetitorsInRaceAndRegattaLog() {
        Competitor c1 = createCompetitor(1);
        Competitor c2 = createCompetitor(2);
        Competitor c3 = createCompetitor(3);

        raceLog.add(new RaceLogRegisterCompetitorEventImpl(now(), author, now(), uuid(), 0, c1));
        regattaLog.add(new RegattaLogRegisterCompetitorEventImpl(now(), author, now(), uuid(), c2));

        raceLog.add(new RaceLogRegisterCompetitorEventImpl(now(), author, now(), uuid(), 0, c3));
        regattaLog.add(new RegattaLogRegisterCompetitorEventImpl(now(), author, now(), uuid(), c3));

        Set<Competitor> result = new MultiLogAnalyzer<>(
                RegisteredCompetitorsAnalyzer.Factory.INSTANCE, new MultiLogAnalyzer.SetReducer<Competitor>(), raceLog,
                regattaLog).analyze();

        assertThat("competitor 1 added", result, hasItem(c1));
        assertThat("competitor 2 added", result, hasItem(c2));
        assertThat("competitor 3 added", result, hasItem(c3));
        assertThat("only three items in total", result.size(), equalTo(3));
    }

    @Test
    public void deviceMappingsInRaceAndRegattaLog() {
        Competitor c1 = createCompetitor(1);
        Competitor c2 = createCompetitor(2);
        
        DeviceIdentifier d1 = createDevice();
        DeviceIdentifier d2 = createDevice();
        
        raceLog.add(new RaceLogDeviceCompetitorMappingEventImpl(now(), author, now(), 0, 0, c1, d1, t(0), t(10)));
        raceLog.add(new RaceLogDeviceCompetitorMappingEventImpl(now(), author, now(), 1, 0, c1, d2, t(30), t(40)));
        regattaLog.add(new RegattaLogDeviceCompetitorMappingEventImpl(now(), author, now(), 2, c2, d2, t(10), t(20)));
        
        // and one for same item (competitor) in other log
        regattaLog.add(new RegattaLogDeviceCompetitorMappingEventImpl(now(), author, now(), 3, c1, d1, t(10),
                t(20)));

        Map<WithID, List<DeviceMapping<WithID>>> result = new MultiLogAnalyzer<>(
                new DeviceMappingFinder.Factory<>(), new MultiLogAnalyzer.MapWithValueCollectionReducer<>(), raceLog,
                regattaLog).analyze();

        assertThat("mappings for two competitors", result.size(), equalTo(2));
        assertThat("three mappings for competitor 1 ", result.get(c1).size(), equalTo(3));
        assertThat("one mapping for competitor 2", result.get(c2).size(), equalTo(1));
    }
}
