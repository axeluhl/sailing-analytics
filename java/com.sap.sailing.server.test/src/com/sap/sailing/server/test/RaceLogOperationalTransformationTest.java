package com.sap.sailing.server.test;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogStartTimeEventImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.operationaltransformation.racelog.RaceLogEventWithTransformationSupport;
import com.sap.sailing.server.operationaltransformation.racelog.RaceLogStartTimeEventWithTransformationSupport;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.operationaltransformation.OperationalTransformer;
import com.sap.sse.operationaltransformation.Peer;
import com.sap.sse.operationaltransformation.PeerImpl;
import com.sap.sse.operationaltransformation.Peer.Role;

public class RaceLogOperationalTransformationTest {
    private RaceLog raceLogClient1;
    private RaceLog raceLogClient2;
    private RaceLog raceLogServer;
    private Peer<RaceLogEventWithTransformationSupport<?>, RaceLog> client1;
    private Peer<RaceLogEventWithTransformationSupport<?>, RaceLog> client2;
    private Peer<RaceLogEventWithTransformationSupport<?>, RaceLog> server;
    private OperationalTransformer<RaceLog, RaceLogEventWithTransformationSupport<?>> transformer;
    private AbstractLogEventAuthor author = new AbstractLogEventAuthorImpl("Test Author", 1);
    private CountDownLatch client1Latch;
    private CountDownLatch client2Latch;
    private CountDownLatch serverLatch;
    
    @Before
    public void setUp() {
        raceLogClient1 = new RaceLogImpl("Test Race Log Client1");
        raceLogClient2 = new RaceLogImpl("Test Race Log Client2");
        raceLogServer = new RaceLogImpl("Test Race Log Server");
        transformer = new OperationalTransformer<>();
        client1Latch = new CountDownLatch(1);
        client1 = new PeerImpl<RaceLogEventWithTransformationSupport<?>, RaceLog>(transformer, raceLogClient1, Role.CLIENT) {
            @Override
            public void apply(Peer<RaceLogEventWithTransformationSupport<?>, RaceLog> source,
                    RaceLogEventWithTransformationSupport<?> operation, int numberOfOperationsSourceHasMergedFromThis) {
                try {
                    client1Latch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                super.apply(source, operation, numberOfOperationsSourceHasMergedFromThis);
            }
        };
        client2Latch = new CountDownLatch(1);
        client2 = new PeerImpl<RaceLogEventWithTransformationSupport<?>, RaceLog>(transformer, raceLogClient2, Role.CLIENT) {
            @Override
            public void apply(Peer<RaceLogEventWithTransformationSupport<?>, RaceLog> source,
                    RaceLogEventWithTransformationSupport<?> operation, int numberOfOperationsSourceHasMergedFromThis) {
                try {
                    client2Latch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                super.apply(source, operation, numberOfOperationsSourceHasMergedFromThis);
            }
        };
        serverLatch = new CountDownLatch(1);
        server = new PeerImpl<RaceLogEventWithTransformationSupport<?>, RaceLog>(transformer, raceLogServer, Role.SERVER) {
            @Override
            public void apply(Peer<RaceLogEventWithTransformationSupport<?>, RaceLog> source,
                    RaceLogEventWithTransformationSupport<?> operation, int numberOfOperationsSourceHasMergedFromThis) {
                try {
                    serverLatch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                super.apply(source, operation, numberOfOperationsSourceHasMergedFromThis);
            }
        };
        server.addPeer(client1);
        client1.addPeer(server);
        server.addPeer(client2);
        client2.addPeer(server);
    }
    
    @Test
    public void testSimpleApply() {
        Calendar c = new GregorianCalendar(2013, 6, 7, 13, 59, 33);
        final MillisecondsTimePoint startTime = new MillisecondsTimePoint(c.getTime());
        RaceLogStartTimeEvent startTimeEvent = new RaceLogStartTimeEventImpl(MillisecondsTimePoint.now(), author, startTime, UUID.randomUUID(), Collections.<Competitor> emptyList(), /* pass ID */ 1, startTime);
        RaceLogStartTimeEventWithTransformationSupport e = new RaceLogStartTimeEventWithTransformationSupport(startTimeEvent);
        server.apply(e);
        serverLatch.countDown();
        client1Latch.countDown();
        client2Latch.countDown();
        waitForEventualConsistency();
        raceLogClient1.lockForRead();
        try {
            assertEquals(1, Util.size(raceLogClient1.getRawFixes()));
        } finally {
            raceLogClient1.unlockAfterRead();
        }
        raceLogClient2.lockForRead();
        try {
            assertEquals(1, Util.size(raceLogClient2.getRawFixes()));
        } finally {
            raceLogClient2.unlockAfterRead();
        }
        raceLogServer.lockForRead();
        raceLogClient1.lockForRead();
        try {
            assertEquals(raceLogServer.getRawFixes().iterator().next(), raceLogClient1.getRawFixes().iterator().next());
        } finally {
            raceLogClient1.unlockAfterRead();
            raceLogServer.unlockAfterRead();
        }
        raceLogServer.lockForRead();
        raceLogClient2.lockForRead();
        try {
            assertEquals(raceLogServer.getRawFixes().iterator().next(), raceLogClient2.getRawFixes().iterator().next());
        } finally {
            raceLogClient2.unlockAfterRead();
            raceLogServer.unlockAfterRead();
        }
    }

    @Test
    public void testConflictingApplyServerWins() {
        Calendar cServer = new GregorianCalendar(2013, 6, 7, 13, 59, 33);
        final MillisecondsTimePoint startTimeServer = new MillisecondsTimePoint(cServer.getTime());
        final MillisecondsTimePoint createdAt = MillisecondsTimePoint.now();
        {
            RaceLogStartTimeEvent startTimeEventServer = new RaceLogStartTimeEventImpl(createdAt,
                    author, startTimeServer, UUID.randomUUID(), Collections.<Competitor> emptyList(), /* pass ID */1,
                    startTimeServer);
            RaceLogStartTimeEventWithTransformationSupport eServer = new RaceLogStartTimeEventWithTransformationSupport(
                    startTimeEventServer);
            server.apply(eServer);
        }
        {
            Calendar cClient1 = new GregorianCalendar(2013, 6, 7, 13, 59, 33);
            final MillisecondsTimePoint startTimeClient1 = new MillisecondsTimePoint(cClient1.getTime());
            RaceLogStartTimeEvent startTimeEventClient1 = new RaceLogStartTimeEventImpl(createdAt,
                    author, startTimeClient1, UUID.randomUUID(), Collections.<Competitor> emptyList(), /* pass ID */1,
                    startTimeClient1);
            final RaceLogStartTimeEventWithTransformationSupport eClient1 = new RaceLogStartTimeEventWithTransformationSupport(
                    startTimeEventClient1);
            client1.apply(eClient1); // this call won't block because server calls apply(Peer, O, int) which first waits for the latch before synchronizing
        }
        serverLatch.countDown();
        client1Latch.countDown();
        client2Latch.countDown();
        waitForEventualConsistency();
        // now assert that the start time is equal on both clients and the server and equal to that set on the server
        raceLogClient1.lockForRead();
        try {
            assertEquals(1, Util.size(raceLogClient1.getRawFixes()));
            assertEquals(startTimeServer, ((RaceLogStartTimeEvent) raceLogClient1.getLastRawFix()).getStartTime());
        } finally {
            raceLogClient1.unlockAfterRead();
        }
        raceLogClient2.lockForRead();
        try {
            assertEquals(1, Util.size(raceLogClient2.getRawFixes()));
            assertEquals(startTimeServer, ((RaceLogStartTimeEvent) raceLogClient2.getLastRawFix()).getStartTime());
        } finally {
            raceLogClient2.unlockAfterRead();
        }
        raceLogServer.lockForRead();
        try {
            assertEquals(startTimeServer, ((RaceLogStartTimeEvent) raceLogServer.getLastRawFix()).getStartTime());
        } finally {
            raceLogServer.unlockAfterRead();
        }
    }

    private void waitForEventualConsistency() {
        server.waitForNotRunning();
        client1.waitForNotRunning();
        client2.waitForNotRunning();
    }
}
