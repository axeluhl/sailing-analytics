package com.sap.sailing.domain.racelog.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.impl.RaceLogEventComparator;

public class RaceLogEventComparatorTest {

    private RaceLogEvent eventOne;
    private RaceLogEvent eventTwo;
    
    private RaceLogEventComparator comparator;
    
    @Before
    public void setUp() {
        eventOne = mock(RaceLogEvent.class);
        eventTwo = mock(RaceLogEvent.class);
        
        comparator = RaceLogEventComparator.INSTANCE;
    }
    
    @Test
    public void testEqualsOnSame() {
        when(eventOne.getTimePoint()).thenReturn(new MillisecondsTimePoint(1));
        when(eventOne.getId()).thenReturn("a");
        
        int result = comparator.compare(eventOne, eventOne);
        assertEquals(0, result);
    }
    
    @Test
    public void testSamePassAndSameTimeSameId() {
        when(eventOne.getPassId()).thenReturn(0);
        when(eventOne.getTimePoint()).thenReturn(new MillisecondsTimePoint(0));
        when(eventOne.getId()).thenReturn("a");
        when(eventTwo.getPassId()).thenReturn(0);
        when(eventTwo.getTimePoint()).thenReturn(new MillisecondsTimePoint(0));
        when(eventTwo.getId()).thenReturn("a");
        
        int result = comparator.compare(eventOne, eventTwo);
        assertEquals(0, result);
    }
    
    @Test
    public void testSamePassAndSameTimeDifferentId() {
        when(eventOne.getPassId()).thenReturn(0);
        when(eventOne.getTimePoint()).thenReturn(new MillisecondsTimePoint(0));
        when(eventOne.getId()).thenReturn("a");
        when(eventTwo.getPassId()).thenReturn(0);
        when(eventTwo.getTimePoint()).thenReturn(new MillisecondsTimePoint(0));
        when(eventTwo.getId()).thenReturn("b");
        
        int result = comparator.compare(eventOne, eventTwo);
        assertTrue(result < 0);
    }
    
    @Test
    public void testSamePassLowerHigherTime() {
        when(eventOne.getPassId()).thenReturn(0);
        when(eventOne.getTimePoint()).thenReturn(new MillisecondsTimePoint(1));
        when(eventTwo.getPassId()).thenReturn(0);
        when(eventTwo.getTimePoint()).thenReturn(new MillisecondsTimePoint(2));
        
        int result = comparator.compare(eventOne, eventTwo);
        assertTrue(result < 0);
    }
    
    @Test
    public void testSamePassHigherLowerTime() {
        when(eventOne.getPassId()).thenReturn(0);
        when(eventOne.getTimePoint()).thenReturn(new MillisecondsTimePoint(2));
        when(eventTwo.getPassId()).thenReturn(0);
        when(eventTwo.getTimePoint()).thenReturn(new MillisecondsTimePoint(1));
        
        int result = comparator.compare(eventOne, eventTwo);
        assertTrue(result > 0);
    }
    
    @Test
    public void testPassLowerHigher() {
        when(eventOne.getPassId()).thenReturn(0);
        when(eventOne.getTimePoint()).thenReturn(new MillisecondsTimePoint(2));
        when(eventTwo.getPassId()).thenReturn(1);
        when(eventTwo.getTimePoint()).thenReturn(new MillisecondsTimePoint(1));
        
        int result = comparator.compare(eventOne, eventTwo);
        assertTrue(result < 0);
    }
    
    @Test
    public void testPassHigherLower() {
        when(eventOne.getPassId()).thenReturn(1);
        when(eventOne.getTimePoint()).thenReturn(new MillisecondsTimePoint(1));
        when(eventTwo.getPassId()).thenReturn(0);
        when(eventTwo.getTimePoint()).thenReturn(new MillisecondsTimePoint(2));
        
        int result = comparator.compare(eventOne, eventTwo);
        assertTrue(result > 0);
    }
    
}
