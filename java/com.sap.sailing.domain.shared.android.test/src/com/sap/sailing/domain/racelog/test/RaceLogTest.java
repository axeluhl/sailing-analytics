package com.sap.sailing.domain.racelog.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.impl.RaceLogImpl;

public class RaceLogTest {
    
    private RaceLog raceLog;
    
    @Before
    public void setUp() {
        raceLog = new RaceLogImpl("testlock");
    }
    
    @Test(expected = IllegalStateException.class)
    public void testThrowsOnNoLock() {
        raceLog.getRawFixes();
    }
    
    @Test
    public void testEmptyOnInitialize() {
        raceLog.lockForRead();
        assertEquals(0, Util.size(raceLog.getRawFixes()));
        raceLog.unlockAfterRead();
    }
    
    @Test
    public void testAddEvent() {
        RaceLogEvent event = mock(RaceLogEvent.class);
        boolean isAdded = raceLog.add(event);
        
        raceLog.lockForRead();
        assertTrue(isAdded);
        assertEquals(event, Util.get(raceLog.getRawFixes(), 0));
        raceLog.unlockAfterRead();
    }
    
    @Test
    public void testAddEventDifferentPassSameTimePoint() {
        RaceLogEvent eventOne = mock(RaceLogEvent.class);
        RaceLogEvent eventTwo = mock(RaceLogEvent.class);
        
        when(eventOne.getPassId()).thenReturn(0);
        when(eventOne.getTimePoint()).thenReturn(new MillisecondsTimePoint(0));
        when(eventTwo.getPassId()).thenReturn(1);
        when(eventTwo.getTimePoint()).thenReturn(new MillisecondsTimePoint(0));
        
        assertTrue(raceLog.add(eventOne));
        assertTrue(raceLog.add(eventTwo));
        
        raceLog.lockForRead();
        assertTrue(Util.contains(raceLog.getRawFixes(), eventOne));
        assertTrue(Util.contains(raceLog.getRawFixes(), eventTwo));
        raceLog.unlockAfterRead();
    }
    
    @Test
    public void testWontAddEventSamePassSameTimePoint() {
        RaceLogEvent eventOne = mock(RaceLogEvent.class);
        RaceLogEvent eventTwo = mock(RaceLogEvent.class);
        
        when(eventOne.getPassId()).thenReturn(0);
        when(eventOne.getTimePoint()).thenReturn(new MillisecondsTimePoint(0));
        when(eventTwo.getPassId()).thenReturn(0);
        when(eventTwo.getTimePoint()).thenReturn(new MillisecondsTimePoint(0));
        
        assertTrue(raceLog.add(eventOne));
        assertFalse(raceLog.add(eventTwo));
        
        raceLog.lockForRead();
        assertTrue(Util.contains(raceLog.getRawFixes(), eventOne));
        assertEquals(1, Util.size(raceLog.getRawFixes()));
        raceLog.unlockAfterRead();
    }
    
    @Test
    public void testAddListener() {
        RaceLogFlagEvent event = mock(RaceLogFlagEvent.class);
        RaceLogEventVisitor listener = mock(RaceLogEventVisitor.class);
        doAnswer(new VisitFlagEventAnswer()).when(event).accept(listener);
        
        raceLog.addListener(listener);
        raceLog.add(event);
        
        verify(listener).visit(event);
    }
    
    @Test
    public void testAddAndRemoveListener() {
        RaceLogFlagEvent event = mock(RaceLogFlagEvent.class);
        RaceLogEventVisitor listener = mock(RaceLogEventVisitor.class);
        doAnswer(new VisitFlagEventAnswer()).when(event).accept(listener);
        
        raceLog.addListener(listener);
        raceLog.removeListener(listener);
        raceLog.add(event);
        
        verify(listener, never()).visit(event);
    }
    
    private class VisitFlagEventAnswer implements Answer<Object> {
        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            RaceLogEventVisitor visitor = (RaceLogEventVisitor) invocation.getArguments()[0];
            RaceLogFlagEvent flagEvent = (RaceLogFlagEvent) invocation.getMock();
            visitor.visit(flagEvent);
            return null;
        }
    }

}
