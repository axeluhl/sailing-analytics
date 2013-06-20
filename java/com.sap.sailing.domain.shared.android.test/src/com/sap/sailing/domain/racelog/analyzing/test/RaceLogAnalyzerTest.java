package com.sap.sailing.domain.racelog.analyzing.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;

import org.junit.Before;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.impl.RaceLogImpl;

public abstract class RaceLogAnalyzerTest<AnalyzerType> {

    protected AnalyzerType analyzer;
    protected RaceLog raceLog;

    @Before
    public void setUpBase() {
        raceLog = new RaceLogImpl("RaceLogTest", "test-identifier");
        analyzer = createAnalyzer(raceLog);
    }
    
    protected abstract AnalyzerType createAnalyzer(RaceLog raceLog);
    
    protected static <T extends RaceLogEvent> T createEvent(Class<T> type, long milliseconds) {
        return createEvent(type, milliseconds, 0, "");
    }
    
    protected static <T extends RaceLogEvent> T createEvent(Class<T> type, long milliseconds, Serializable id) {
        return createEvent(type, milliseconds, 0, id);
    }
    
    protected static <T extends RaceLogEvent> T createEvent(Class<T> type, long milliseconds, int passId, Serializable id) {
        T event = mock(type);
        when(event.getPassId()).thenReturn(passId);
        when(event.getTimePoint()).thenReturn(new MillisecondsTimePoint(milliseconds));
        when(event.getId()).thenReturn(id);
        return event;
    }

}
