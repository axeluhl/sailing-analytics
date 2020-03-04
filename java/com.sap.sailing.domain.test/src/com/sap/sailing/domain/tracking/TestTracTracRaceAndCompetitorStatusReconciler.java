package com.sap.sailing.domain.tracking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogFlagEvent;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.AbortingFlagFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogPassChangeEventImpl;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.impl.RaceAndCompetitorStatusWithRaceLogReconciler;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.tractrac.model.lib.api.event.IRace;
import com.tractrac.model.lib.api.event.RaceStatusType;

/**
 * See also bug 5154 and {@link RaceAndCompetitorStatusWithRaceLogReconciler}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class TestTracTracRaceAndCompetitorStatusReconciler {
    private AbstractLogEventAuthor author;
    private TrackedRace trackedRace;
    private IRace tractracRace;
    private RaceLog raceLog;
    private RaceAndCompetitorStatusWithRaceLogReconciler reconciler;
    private TimePoint startOfPass;
    
    @Before
    public void setUp() {
        author = new LogEventAuthorImpl("me", 1);
        startOfPass = MillisecondsTimePoint.now();
        tractracRace = mock(IRace.class);
        trackedRace = mock(TrackedRace.class);
        raceLog = new RaceLogImpl("RaceLogID");
        when(trackedRace.getAttachedRaceLogs()).thenReturn(Collections.singleton(raceLog));
        reconciler = new RaceAndCompetitorStatusWithRaceLogReconciler(DomainFactory.INSTANCE, new RaceLogResolver() {
            @Override
            public RaceLog resolve(SimpleRaceLogIdentifier identifier) {
                return raceLog;
            }
        });
        raceLog.add(new RaceLogPassChangeEventImpl(startOfPass, author, /* pPassId */ 1));
    }
    
    @Test
    public void testAbandonInFirstPass() {
        assertNull(new AbortingFlagFinder(raceLog).analyze());
        when(tractracRace.getStatus()).thenReturn(RaceStatusType.ABANDONED);
        final TimePoint abandonTimePoint = startOfPass.plus(Duration.ONE_MINUTE);
        when(tractracRace.getStatusTime()).thenReturn(abandonTimePoint.asMillis());
        reconciler.reconcileRaceStatus(tractracRace, trackedRace);
        final RaceLogFlagEvent abandonFlagEvent = new AbortingFlagFinder(raceLog).analyze();
        assertNotNull(abandonFlagEvent);
        assertSame(Flags.NOVEMBER, abandonFlagEvent.getUpperFlag());
        assertTrue(abandonFlagEvent.isDisplayed());
        assertEquals(abandonTimePoint, abandonFlagEvent.getLogicalTimePoint());
    }
}
