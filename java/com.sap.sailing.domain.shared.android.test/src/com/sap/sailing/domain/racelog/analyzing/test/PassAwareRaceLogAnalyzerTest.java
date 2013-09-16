package com.sap.sailing.domain.racelog.analyzing.test;

import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Test;

import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.analyzing.impl.RaceLogAnalyzer;

public abstract class PassAwareRaceLogAnalyzerTest<AnalyzerType extends RaceLogAnalyzer<ResultType>, ResultType>
        extends RaceLogAnalyzerTest<AnalyzerType> {

    protected abstract ResultType setupTargetEventsForPassAwareTests(int passId);

    @Test
    public void testPassAwareWrongPass() {
        ResultType nonExpectedResult = setupTargetEventsForPassAwareTests(0);
        RaceLogEvent blockingEvent = createEvent(RaceLogEvent.class, 1, 1, UUID.randomUUID());
        
        raceLog.add(blockingEvent);

        assertNotSame(nonExpectedResult, analyzer.analyze());
    }

    @Test
    public void testPassAwareCorrectPass() {
        RaceLogEvent minorEvent = createEvent(RaceLogEvent.class, 0, 0, UUID.randomUUID());
        ResultType expectedResult = setupTargetEventsForPassAwareTests(1);

        raceLog.add(minorEvent);

        assertSame(expectedResult, analyzer.analyze());
    }
}
