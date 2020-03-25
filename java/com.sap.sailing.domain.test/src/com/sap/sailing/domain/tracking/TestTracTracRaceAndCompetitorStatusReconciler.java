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
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogFlagEventImpl;
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
        }, tractracRace);
        raceLog.add(new RaceLogPassChangeEventImpl(startOfPass, author, /* pPassId */ 1));
    }
    
    @Test
    public void testAbandonInFirstPassAndRestart() {
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
        // now change status back to START
        when(tractracRace.getStatus()).thenReturn(RaceStatusType.START);
        final TimePoint restartTimePoint = startOfPass.plus(Duration.ONE_MINUTE.times(2));
        when(tractracRace.getStatusTime()).thenReturn(restartTimePoint.asMillis());
        reconciler.reconcileRaceStatus(tractracRace, trackedRace);
        final RaceLogFlagEvent newAbandonFlagEvent = new AbortingFlagFinder(raceLog).analyze();
        assertNull(newAbandonFlagEvent);
        assertEquals(3, raceLog.getCurrentPassId()); // abandoning also creates the next pass immediately
        reconciler.reconcileRaceStatus(tractracRace, trackedRace);
        assertEquals(3, raceLog.getCurrentPassId()); // assert that reconciliation is idempotent and does not add another pass
    }

    @Test
    public void testManualAbandonButLaterTracTracGeneralRecall() {
        final TimePoint manualAbortTimePoint = startOfPass.plus(Duration.ONE_MINUTE);
        raceLog.add(new RaceLogFlagEventImpl(manualAbortTimePoint, author, /* pass id */ 1, Flags.NOVEMBER, /* lower flag */ null, /* isDisplayed */ true));
        raceLog.add(new RaceLogPassChangeEventImpl(manualAbortTimePoint, author, /* pass id */ 2));
        when(tractracRace.getStatus()).thenReturn(RaceStatusType.GENERAL_RECALL);
        final TimePoint generalRecallTimePoint = manualAbortTimePoint.plus(Duration.ONE_MINUTE);
        when(tractracRace.getStatusTime()).thenReturn(generalRecallTimePoint.asMillis());
        reconciler.reconcileRaceStatus(tractracRace, trackedRace);
        final AbortingFlagFinder abortingFlagFinder = new AbortingFlagFinder(raceLog);
        final RaceLogFlagEvent generalRecallFlagEvent = abortingFlagFinder.analyze();
        assertNotNull(generalRecallFlagEvent);
        assertSame(Flags.FIRSTSUBSTITUTE, generalRecallFlagEvent.getUpperFlag());
        assertTrue(generalRecallFlagEvent.isDisplayed());
        assertEquals(generalRecallTimePoint, generalRecallFlagEvent.getLogicalTimePoint());
        assertEquals(2, generalRecallFlagEvent.getPassId());
        assertEquals(3, raceLog.getCurrentPassId());
        reconciler.reconcileRaceStatus(tractracRace, trackedRace); // assert that reconciliation is idempotent
        assertEquals(3, raceLog.getCurrentPassId());
        assertSame(generalRecallFlagEvent, abortingFlagFinder.analyze());
    }

    @Test
    public void testManualAbandonAndEarlierTracTracGeneralRecallWillBeIgnored() {
        final TimePoint manualAbortTimePoint = startOfPass.plus(Duration.ONE_MINUTE);
        raceLog.add(new RaceLogFlagEventImpl(manualAbortTimePoint, author, /* pass id */ 1, Flags.NOVEMBER, /* lower flag */ null, /* isDisplayed */ true));
        raceLog.add(new RaceLogPassChangeEventImpl(manualAbortTimePoint, author, /* pass id */ 2));
        when(tractracRace.getStatus()).thenReturn(RaceStatusType.GENERAL_RECALL);
        final TimePoint generalRecallTimePoint = manualAbortTimePoint.minus(Duration.ONE_MINUTE.times(2));
        when(tractracRace.getStatusTime()).thenReturn(generalRecallTimePoint.asMillis());
        reconciler.reconcileRaceStatus(tractracRace, trackedRace);
        final AbortingFlagFinder abortingFlagFinder = new AbortingFlagFinder(raceLog);
        final RaceLogFlagEvent abortFlagEvent = abortingFlagFinder.analyze();
        assertNotNull(abortFlagEvent);
        assertSame(Flags.NOVEMBER, abortFlagEvent.getUpperFlag());
        assertTrue(abortFlagEvent.isDisplayed());
        assertEquals(manualAbortTimePoint, abortFlagEvent.getLogicalTimePoint());
        assertEquals(1, abortFlagEvent.getPassId());
        assertEquals(2, raceLog.getCurrentPassId());
        reconciler.reconcileRaceStatus(tractracRace, trackedRace); // assert that reconciliation is idempotent
        assertEquals(2, raceLog.getCurrentPassId());
        assertSame(abortFlagEvent, abortingFlagFinder.analyze());
    }
}
