package com.sap.sailing.domain.tracking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.race.CompetitorResult;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogFlagEvent;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.AbortingFlagFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogFlagEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogPassChangeEventImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.impl.RaceAndCompetitorStatusWithRaceLogReconciler;
import com.sap.sse.common.Color;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.tractrac.model.lib.api.event.ICompetitor;
import com.tractrac.model.lib.api.event.IRace;
import com.tractrac.model.lib.api.event.IRaceCompetitor;
import com.tractrac.model.lib.api.event.RaceCompetitorStatusType;
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
    private RaceAndCompetitorStatusWithRaceLogReconcilerWithPublicResultFetcher reconciler;
    private TimePoint startOfPass;
    private IRaceCompetitor tractracRaceCompetitor;
    private ICompetitor tractracCompetitor;
    private Competitor competitor;
    
    private static class RaceAndCompetitorStatusWithRaceLogReconcilerWithPublicResultFetcher extends RaceAndCompetitorStatusWithRaceLogReconciler {
        public RaceAndCompetitorStatusWithRaceLogReconcilerWithPublicResultFetcher(DomainFactory domainFactory,
                RaceLogResolver raceLogResolver, IRace tractracRace) {
            super(domainFactory, raceLogResolver, tractracRace);
        }

        @Override
        public Pair<CompetitorResult, TimePoint> getRaceLogResultAndCreationTimePointForCompetitor(
                TrackedRace trackedRace, Competitor competitor) {
            return super.getRaceLogResultAndCreationTimePointForCompetitor(trackedRace, competitor);
        }
    }
    
    @Before
    public void setUp() {
        author = new LogEventAuthorImpl("me", 1);
        startOfPass = MillisecondsTimePoint.now();
        tractracRace = mock(IRace.class);
        when(tractracRace.getStatus()).thenReturn(RaceStatusType.RACING);
        trackedRace = mock(TrackedRace.class);
        tractracCompetitor = mock(ICompetitor.class);
        tractracRaceCompetitor = mock(IRaceCompetitor.class);
        when(tractracRaceCompetitor.getRace()).thenReturn(tractracRace);
        when(tractracRaceCompetitor.getCompetitor()).thenReturn(tractracCompetitor);
        final String competitorName = "The Competitor";
        final UUID competitorId = UUID.randomUUID();
        DynamicBoat b = new BoatImpl(competitorId, competitorName + "'s boat", new BoatClassImpl("505", /* typicallyStartsUpwind */true), null, null);
        competitor = DomainFactory.INSTANCE.getBaseDomainFactory().getOrCreateCompetitorWithBoat(
                competitorId, competitorName, "TC", Color.RED, null, null, new TeamImpl("STG", Collections.singleton(
                        new PersonImpl(competitorName, new NationalityImpl("GER"),
                        /* dateOfBirth */null, "This is famous " + competitorName)), new PersonImpl("Rigo van Maas",
                        new NationalityImpl("NED"),
                        /* dateOfBirth */null, "This is Rigo, the coach")), 
                        /* timeOnTimeFactor */ null, /* timeOnDistanceAllowancePerNauticalMile */ null, null, b, /* store */ false);
        when(tractracCompetitor.getId()).thenReturn((UUID) competitor.getId());
        raceLog = new RaceLogImpl("RaceLogID");
        when(trackedRace.getAttachedRaceLogs()).thenReturn(Collections.singleton(raceLog));
        reconciler = new RaceAndCompetitorStatusWithRaceLogReconcilerWithPublicResultFetcher(DomainFactory.INSTANCE, new RaceLogResolver() {
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

    @Test
    public void testIRMUpdateFromTracTracMapsToRaceLogCompetitorResult() {
        // emulate we received a BFD for a competitor a second after the start of the pass
        final long resultTimePoint = startOfPass.plus(Duration.ONE_SECOND).asMillis();
        when(tractracRaceCompetitor.getStatusTime()).thenReturn(resultTimePoint);
        when(tractracRaceCompetitor.getStatus()).thenReturn(RaceCompetitorStatusType.BFD);
        reconciler.reconcileCompetitorStatus(tractracRaceCompetitor, trackedRace);
        {
            final Pair<CompetitorResult, TimePoint> raceLogBasedResult = reconciler.getRaceLogResultAndCreationTimePointForCompetitor(trackedRace, competitor);
            assertNotNull(raceLogBasedResult);
            assertEquals(resultTimePoint, raceLogBasedResult.getB());
            assertEquals(MaxPointsReason.BFD, raceLogBasedResult.getA().getMaxPointsReason());
        }
        // now simulate an "outdated" TracTrac event and assert that it has no impact on a newer result:
        final long outdatedResultTimePoint = startOfPass.plus(Duration.ONE_SECOND.divide(2)).asMillis();
        when(tractracRaceCompetitor.getStatusTime()).thenReturn(outdatedResultTimePoint);
        when(tractracRaceCompetitor.getStatus()).thenReturn(RaceCompetitorStatusType.DNF);
        {
            final Pair<CompetitorResult, TimePoint> raceLogBasedResult = reconciler.getRaceLogResultAndCreationTimePointForCompetitor(trackedRace, competitor);
            assertNotNull(raceLogBasedResult);
            assertEquals(resultTimePoint, raceLogBasedResult.getB()); // still expecting to see the result from the later time point
            assertEquals(MaxPointsReason.BFD, raceLogBasedResult.getA().getMaxPointsReason());
        }
        // now simulate a newer TracTrac event and assert that it updates the result:
        final long newerResultTimePoint = startOfPass.plus(Duration.ONE_SECOND.times(2)).asMillis();
        when(tractracRaceCompetitor.getStatusTime()).thenReturn(newerResultTimePoint);
        when(tractracRaceCompetitor.getStatus()).thenReturn(RaceCompetitorStatusType.DNC);
        {
            final Pair<CompetitorResult, TimePoint> raceLogBasedResult = reconciler.getRaceLogResultAndCreationTimePointForCompetitor(trackedRace, competitor);
            assertNotNull(raceLogBasedResult);
            assertEquals(newerResultTimePoint, raceLogBasedResult.getB()); // still expecting to see the result from the later time point
            assertEquals(MaxPointsReason.DNC, raceLogBasedResult.getA().getMaxPointsReason());
        }
    }

    @Test
    public void testOfficialRankUpdateToRaceLogCompetitorResult() {
        // emulate we received a valid official rank for a competitor a second after the start of the pass
        final long resultTimePoint = startOfPass.plus(Duration.ONE_SECOND).asMillis();
        when(tractracRaceCompetitor.getStatusTime()).thenReturn(resultTimePoint);
        when(tractracRaceCompetitor.getOfficialRank()).thenReturn(42);
        reconciler.reconcileCompetitorStatus(tractracRaceCompetitor, trackedRace);
        {
            final Pair<CompetitorResult, TimePoint> raceLogBasedResult = reconciler.getRaceLogResultAndCreationTimePointForCompetitor(trackedRace, competitor);
            assertNotNull(raceLogBasedResult);
            assertEquals(resultTimePoint, raceLogBasedResult.getB());
            assertEquals(MaxPointsReason.NONE, raceLogBasedResult.getA().getMaxPointsReason());
            assertEquals(42, raceLogBasedResult.getA().getOneBasedRank());
        }
        // now simulate an "outdated" TracTrac event and assert that it has no impact on a newer result:
        final long outdatedResultTimePoint = startOfPass.plus(Duration.ONE_SECOND.divide(2)).asMillis();
        when(tractracRaceCompetitor.getStatusTime()).thenReturn(outdatedResultTimePoint);
        when(tractracRaceCompetitor.getOfficialRank()).thenReturn(43);
        {
            final Pair<CompetitorResult, TimePoint> raceLogBasedResult = reconciler.getRaceLogResultAndCreationTimePointForCompetitor(trackedRace, competitor);
            assertNotNull(raceLogBasedResult);
            assertEquals(resultTimePoint, raceLogBasedResult.getB());
            assertEquals(MaxPointsReason.NONE, raceLogBasedResult.getA().getMaxPointsReason());
            assertEquals(42, raceLogBasedResult.getA().getOneBasedRank());
        }
        // now simulate a newer TracTrac event and assert that it updates the result:
        final long newerResultTimePoint = startOfPass.plus(Duration.ONE_SECOND.times(2)).asMillis();
        when(tractracRaceCompetitor.getStatusTime()).thenReturn(newerResultTimePoint);
        when(tractracRaceCompetitor.getOfficialRank()).thenReturn(44);
        {
            final Pair<CompetitorResult, TimePoint> raceLogBasedResult = reconciler.getRaceLogResultAndCreationTimePointForCompetitor(trackedRace, competitor);
            assertNotNull(raceLogBasedResult);
            assertEquals(newerResultTimePoint, raceLogBasedResult.getB());
            assertEquals(MaxPointsReason.NONE, raceLogBasedResult.getA().getMaxPointsReason());
            assertEquals(44, raceLogBasedResult.getA().getOneBasedRank());
        }
        // now simulate a yet newer TracTrac event that resets the official rank to 0, stating that there is no official rank yet
        final long yetNewerResultTimePoint = startOfPass.plus(Duration.ONE_SECOND.times(2)).asMillis();
        when(tractracRaceCompetitor.getStatusTime()).thenReturn(yetNewerResultTimePoint);
        when(tractracRaceCompetitor.getOfficialRank()).thenReturn(0);
        {
            final Pair<CompetitorResult, TimePoint> raceLogBasedResult = reconciler.getRaceLogResultAndCreationTimePointForCompetitor(trackedRace, competitor);
            assertNotNull(raceLogBasedResult);
            assertEquals(yetNewerResultTimePoint, raceLogBasedResult.getB());
            assertEquals(MaxPointsReason.NONE, raceLogBasedResult.getA().getMaxPointsReason());
            assertEquals(0, raceLogBasedResult.getA().getOneBasedRank());
        }
    }

}
