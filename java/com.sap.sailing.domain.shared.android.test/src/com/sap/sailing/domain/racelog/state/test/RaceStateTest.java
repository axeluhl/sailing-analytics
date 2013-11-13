package com.sap.sailing.domain.racelog.state.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.impl.RaceLogEventAuthorImpl;
import com.sap.sailing.domain.racelog.impl.RaceLogImpl;
import com.sap.sailing.domain.racelog.state.RaceState2;
import com.sap.sailing.domain.racelog.state.RaceState2ChangedListener;
import com.sap.sailing.domain.racelog.state.impl.RaceState2Impl;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure2;

public class RaceStateTest {
    
    private RaceLog raceLog;
    private RaceLogEventAuthor author;
    private RaceLogEventFactory factory;
    private RacingProcedureType defaultRacingProcedureType;
    private RaceState2ChangedListener listener;
    
    private RaceState2 state;
    
    @Before
    public void setUp() {
        raceLog = new RaceLogImpl("test-log");
        author = new RaceLogEventAuthorImpl("Test", 1);
        factory = RaceLogEventFactory.INSTANCE;
        defaultRacingProcedureType = RacingProcedureType.RRS26;
        listener = mock(RaceState2ChangedListener.class);
        
        state = new RaceState2Impl(raceLog, author, factory, defaultRacingProcedureType);
    }
    
    @Test
    public void testStateOnEmptyRaceLog() {
        assertNull(state.getCourseDesign());
        assertNull(state.getFinishedTime());
        assertNull(state.getFinishingTime());
        assertNull(state.getFinishPositioningList());
        assertNull(state.getProtestTime());
        assertNull(state.getStartTime());
        assertNotNull(state.getRacingProcedure());
        assertEquals(defaultRacingProcedureType, state.getRacingProcedure().getType());
        assertEquals(RaceLogRaceStatus.UNSCHEDULED, state.getStatus());
        assertFalse(state.isFinishPositioningConfirmed());
    }
    
    @Test
    public void testStartTime() {
        state.addChangedListener(listener);
        
        TimePoint startTime = MillisecondsTimePoint.now().plus(60 * 60 * 1000);
        state.setStartTime(startTime);
        
        assertEquals(startTime, state.getStartTime());
        assertEquals(RaceLogRaceStatus.SCHEDULED, state.getStatus());
        verify(listener).onStartTimeChanged(state);
        verify(listener).onStatusChanged(state);
        verifyNoMoreInteractions(listener);
    }
    
    @Test
    public void testAdvancePass() {
        state.addChangedListener(listener);
        int oldPassId = raceLog.getCurrentPassId();
        
        state.setAdvancePass(mock(TimePoint.class));
        
        assertEquals(oldPassId + 1, raceLog.getCurrentPassId());
        verify(listener).onAdvancePass(state);
        verifyNoMoreInteractions(listener);
    }
    
    @Test
    public void testAbort() {
        state.addChangedListener(listener);
        
        state.setAborted(mock(TimePoint.class), false, Flags.NONE);
        
        verify(listener).onAdvancePass(state);
        verifyNoMoreInteractions(listener);
    }
    
    @Test
    public void testGeneralRecall() {
        state.addChangedListener(listener);
        
        state.setGeneralRecall(mock(TimePoint.class));
        
        verify(listener).onAdvancePass(state);
        verifyNoMoreInteractions(listener);
    }
    
    @Test
    public void testInvalidateAfterAdvancePass() throws InterruptedException {
        state.addChangedListener(listener);
        
        state.setStartTime(new MillisecondsTimePoint(1));
        Thread.sleep(100);
        state.setFinishedTime(new MillisecondsTimePoint(10));
        state.setCourseDesign(mock(CourseBase.class));
        
        state.setAdvancePass(mock(TimePoint.class));
        
        assertNull(state.getStartTime());
        assertEquals(RaceLogRaceStatus.UNSCHEDULED, state.getStatus());
        verify(listener, times(1)).onAdvancePass(state);
        verify(listener, times(2)).onStartTimeChanged(state);
        verify(listener, times(2)).onFinishedTimeChanged(state);
        verify(listener, times(3)).onStatusChanged(state);
        verify(listener, times(1)).onCourseDesignChanged(state);
        verifyNoMoreInteractions(listener);
    }
    
    @Test
    public void testNewRacingProcedure() throws InterruptedException {
        state.addChangedListener(listener);
        
        RacingProcedure2 oldProcedure = state.getRacingProcedure();
        assertEquals(defaultRacingProcedureType, oldProcedure.getType());
        
        state.setRacingProcedure(RacingProcedureType.ESS);
        
        RacingProcedure2 newProcedure = state.getRacingProcedure();
        assertEquals(RacingProcedureType.ESS, newProcedure.getType());
        assertFalse(oldProcedure.equals(newProcedure));
    }

}
