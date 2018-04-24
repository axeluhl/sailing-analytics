package com.sap.sailing.domain.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.function.Consumer;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.racelogtracking.impl.fixtracker.RegattaLogFixTrackerRegattaListener;
import com.sap.sailing.domain.ranking.OneDesignRankingMetric;
import com.sap.sailing.domain.regattalog.impl.EmptyRegattaLogStore;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRegattaImpl;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sse.util.ThreadLocalTransporter;

public class TestDeadlockInRegattaListener {
    @Rule
    public Timeout globalTimeout = new Timeout(5000); // fail after 1s

    @Test
    public void testDeadlockInRegattaListener() throws InterruptedException, BrokenBarrierException, MalformedURLException, IOException {
        CyclicBarrier latch = new CyclicBarrier(2);
        CyclicBarrier monitorOnRegattaListenerLatch = new CyclicBarrier(2);
        @SuppressWarnings("unchecked")
        ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker =
                (ServiceTracker<RacingEventService, RacingEventService>) mock(ServiceTracker.class);
        final String regattaName = "Test Regatta";
        final RegattaImpl regatta = new RegattaImpl(
                /* raceLogStore */ null, EmptyRegattaLogStore.INSTANCE, regattaName,
                new BoatClassImpl("49er", true), /* can boats change */ false, /* startDate */ null, /* endDate */ null,
                /* trackedRegattaRegistry */ null, new LowPoint(), UUID.randomUUID(),
                /* courseArea */ null, /* controlTrackingFromStartAndFinishTimes */ true,
                OneDesignRankingMetric::new);
        DynamicTrackedRegatta trackedRegatta = new DynamicTrackedRegattaImpl(regatta) {
            private static final long serialVersionUID = -3599667964201700780L;

            @Override
            protected void notifyListenersAboutTrackedRaceRemoved(TrackedRace trackedRace,
                    Optional<ThreadLocalTransporter> threadLocalTransporter) {
                try {
                    latch.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    throw new RuntimeException(e);
                }
                super.notifyListenersAboutTrackedRaceRemoved(trackedRace, Optional.empty());
            }
        };
        RacingEventServiceImpl racingEventService = new RacingEventServiceImpl() {
            @Override
            public void getRaceTrackerByRegattaAndRaceIdentifier(RegattaAndRaceIdentifier raceIdentifier,
                    Consumer<RaceTracker> callback) {
                // called by RegattaLogFixTrackerRegattaListener.onRaceAdded while in synchronized block of RegattaListener
                try {
                    monitorOnRegattaListenerLatch.await(); // allow controller to stop us here, thus waiting until we own the RegattaListener monitor
                    latch.await(); // wait until TrackedRegattaImpl, holding its trackedRacesLock's write lock, tries to notify its listeners about
                    // the tracked race's removal
                } catch (InterruptedException | BrokenBarrierException e) {
                    throw new RuntimeException(e);
                }
                getExistingTrackedRace(new RegattaNameAndRaceName(regattaName, "b")); // no matter which; it requests the read lock on TrackedRegattaImpl.trackedRacesLock
            }
            
            @Override
            public DynamicTrackedRegatta getOrCreateTrackedRegatta(Regatta r) {
                if (r == regatta) {
                    return trackedRegatta;
                } else {
                    return super.getOrCreateTrackedRegatta(r);
                }
            }
            @Override
            public DynamicTrackedRegatta getTrackedRegatta(Regatta r) {
                if (r == regatta) {
                    return trackedRegatta;
                } else {
                    return super.getTrackedRegatta(regatta);
                }
            }
        };
        when(racingEventServiceTracker.getService()).thenReturn(racingEventService);
        RegattaLogFixTrackerRegattaListener listener = new RegattaLogFixTrackerRegattaListener(
                racingEventServiceTracker, null);

        racingEventService.addRegattaWithoutReplication(trackedRegatta.getRegatta());
        String raceName1 = "R1";
        String raceName2 = "R2";
        TrackedRace trackedRace1 = mock(DynamicTrackedRaceImpl.class);
        RaceDefinition race1 = mock(RaceDefinition.class);
        when(race1.getName()).thenReturn(raceName1);
        when(race1.getCourse()).thenReturn(mock(Course.class));
        when(trackedRace1.getRace()).thenReturn(race1);
        TrackedRace trackedRace2 = mock(DynamicTrackedRaceImpl.class);
        RaceDefinition race2 = mock(RaceDefinition.class);
        when(race2.getCourse()).thenReturn(mock(Course.class));
        when(race2.getName()).thenReturn(raceName2);
        when(trackedRace2.getRace()).thenReturn(race2);
        when(trackedRace1.getRaceIdentifier()).thenReturn(new RegattaNameAndRaceName(regattaName, raceName1));
        when(trackedRace2.getRaceIdentifier()).thenReturn(new RegattaNameAndRaceName(regattaName, raceName2));
        listener.regattaAdded(trackedRegatta);
        new Thread(()->{
            try {
                monitorOnRegattaListenerLatch.await(); // let the first addTrackedRace pass through
                latch.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
        }).start();
        trackedRegatta.addTrackedRace(trackedRace1, Optional.empty());
        // the following runs into RacingEventService.getRaceTrackerByRegattaAndRaceIdentifier
        // which waits for the latch based on the override above while in synchronized RegattaListener.raceAdded
        new Thread(()->trackedRegatta.addTrackedRace(trackedRace2, Optional.empty())).start();
        monitorOnRegattaListenerLatch.await();
        // the following awaits the latch in TrackedRegattaImpl.notifyListenersAboutTrackedRaceRemoved
        // after the write lock has been obtained but before the synchronized RegattaListener.raceRemoved method
        // is called. Before the fix for bug4414 the TrackedRegattaImpl.removeTrackedRace method calls the
        // listeners while still holding the write lock. 
        racingEventService.removeRace(regatta, trackedRace1.getRace());
    }
}
