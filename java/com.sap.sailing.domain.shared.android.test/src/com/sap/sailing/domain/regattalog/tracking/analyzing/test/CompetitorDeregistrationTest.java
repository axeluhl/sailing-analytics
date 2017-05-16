package com.sap.sailing.domain.regattalog.tracking.analyzing.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Collections;
import java.util.Set;

import org.junit.Test;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogRegisterCompetitorEventImpl;
import com.sap.sailing.domain.abstractlog.shared.analyzing.CompetitorDeregistrator;
import com.sap.sailing.domain.abstractlog.shared.analyzing.CompetitorsInLogAnalyzer;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class CompetitorDeregistrationTest extends AbstractRegattaLogTrackingTest {
    private final Competitor competitor = new CompetitorImpl("comp", "Comp", null, null, null, null, null, /* timeOnTimeFactor */ null, /* timeOnDistanceAllowancePerNauticalMile */ null, null);
    private final Competitor competitor2 = new CompetitorImpl("comp2", "Comp2", null, null, null, null, null, /* timeOnTimeFactor */ null, /* timeOnDistanceAllowancePerNauticalMile */ null, null);
    
    @Test
    public void testCompetitorDeregistration() {
        log.add(new RegattaLogRegisterCompetitorEventImpl(MillisecondsTimePoint.now(), author, competitor));
        log.add(new RegattaLogRegisterCompetitorEventImpl(MillisecondsTimePoint.now(), author, competitor2));
        final CompetitorDeregistrator<RegattaLog, RegattaLogEvent, RegattaLogEventVisitor> deregistrator = new CompetitorDeregistrator<>(log,
                Collections.singleton(competitor2), author);
        final Set<RegattaLogEvent> events = deregistrator.analyze();
        deregistrator.deregister(events);
        final Set<Competitor> competitors = new CompetitorsInLogAnalyzer<>(log).analyze();
        assertEquals(1, competitors.size());
        assertSame(competitor, competitors.iterator().next());
    }
}
