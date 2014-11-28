package com.sap.sailing.domain.racelog.tracking.analyzing.test;

import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRevokeEvent;
import com.sap.sse.common.Util;

public class RevokeEventTest extends AbstractRaceLogTrackingTest {
    @Test
    public void revokedEventIsExcluded() {
        RaceLogEvent event = factory.createDeviceCompetitorMappingEvent(now, author, now, UUID.randomUUID(), null, null, 0, null, null);
        RaceLogRevokeEvent revokeEvent = factory.createRevokeEvent(now.plus(1), author, now.plus(1), UUID.randomUUID(), 0, event.getId(),
                null, null, null);
        
        log.add(event);
        log.add(revokeEvent);
        
        log.lockForRead();
        Assert.assertFalse(Util.contains(log.getUnrevokedEvents(), event));
        log.unlockAfterRead();
    }
    
    @Test
    public void eventRevokedByAuthorWithLowerPrioIsNotExcluded() {
    	RaceLogEvent event = factory.createDeviceCompetitorMappingEvent(now, author, now, UUID.randomUUID(), null, null, 0, null, null);
    	RaceLogRevokeEvent revokeEvent = factory.createRevokeEvent(now.plus(1), author1, now.plus(1), UUID.randomUUID(), 0, event.getId(),
                null, null, null);
        
        log.add(event);
        log.add(revokeEvent);
        
        log.lockForRead();
        Assert.assertTrue(Util.contains(log.getUnrevokedEvents(), event));
        log.unlockAfterRead();
    }
    
    @Test
    public void revokingRevokeEventIsNotHarmful() {
        RaceLogEvent event = factory.createDeviceCompetitorMappingEvent(now, author, now, UUID.randomUUID(), null, null, 0, null, null);
        RaceLogRevokeEvent revokeEvent = factory.createRevokeEvent(now.plus(1), author, now.plus(1), UUID.randomUUID(), 0, event.getId(),
                null, null, null);
        RaceLogRevokeEvent revokeEvent2 = factory.createRevokeEvent(now.plus(2), author, now.plus(2), UUID.randomUUID(), 2, revokeEvent.getId(),
                null, null, null);
        
        log.add(event);
        log.add(revokeEvent);
        
        log.lockForRead();
        Assert.assertFalse(Util.contains(log.getUnrevokedEvents(), event));
        Assert.assertFalse(Util.contains(log.getUnrevokedEvents(), revokeEvent));
        Assert.assertFalse(Util.contains(log.getUnrevokedEvents(), revokeEvent2));
        log.unlockAfterRead();
    }
}
