package com.sap.sailing.domain.racelog.analyzing.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.UUID;

import org.junit.Before;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.analyzing.impl.RaceLogAnalyzer;
import com.sap.sailing.domain.racelog.impl.RaceLogImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public abstract class RaceLogAnalyzerTest<AnalyzerType extends RaceLogAnalyzer<?>> {

    protected AnalyzerType analyzer;
    protected RaceLog raceLog;

    @Before
    public void setUpBase() {
        raceLog = new RaceLogImpl("RaceLogTest", "test-identifier");
        analyzer = createAnalyzer(raceLog);
    }
    
    protected abstract AnalyzerType createAnalyzer(RaceLog raceLog);
    
    protected static <T extends RaceLogEvent> T createEvent(Class<T> type, long milliseconds) {
        return createEvent(type, milliseconds, 0, UUID.randomUUID());
    }
    
    protected static <T extends RaceLogEvent> T createEvent(Class<T> type, long milliseconds, Serializable id) {
        return createEvent(type, milliseconds, 0, id);
    }
    
    protected static <T extends RaceLogEvent> T createEvent(Class<T> type, long milliseconds, int passId) {
        return createEvent(type, milliseconds, passId, UUID.randomUUID());
    }
    
    protected static <T extends RaceLogEvent> T createEvent(Class<T> type, long milliseconds, int passId, Serializable id) {
        return createEvent(type, milliseconds, passId, id, mock(RaceLogEventAuthor.class));
    }
    
    protected static <T extends RaceLogEvent> T createEvent(Class<T> type, long milliseconds, int passId, RaceLogEventAuthor author) {
        return createEvent(type, milliseconds, passId, UUID.randomUUID(), author);
    }
    
    protected static <T extends RaceLogEvent> T createEvent(Class<T> type, long milliseconds, int passId, Serializable id, RaceLogEventAuthor author) {
        T event = mock(type);
        when(event.getAuthor()).thenReturn(author);
        when(event.getPassId()).thenReturn(passId);
        when(event.getCreatedAt()).thenReturn(new MillisecondsTimePoint(milliseconds));
        when(event.getLogicalTimePoint()).thenReturn(new MillisecondsTimePoint(milliseconds));
        when(event.getId()).thenReturn(id);
        return event;
    }

}
