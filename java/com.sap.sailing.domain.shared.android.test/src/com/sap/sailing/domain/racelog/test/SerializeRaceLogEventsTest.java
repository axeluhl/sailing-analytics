package com.sap.sailing.domain.racelog.test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventFactory;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.DeviceCompetitorMappingEvent;
import com.sap.sailing.domain.racelog.tracking.test.mock.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.test.AbstractLeaderboardTest;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * Perform a few tests around race log and race log event serialization / deserialization
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class SerializeRaceLogEventsTest {
    private ObjectOutputStream oos;
    private ByteArrayOutputStream bos;
    private RaceLog raceLog;
    private RaceLogEventFactory eventFactory;
    
    @Before
    public void setUp() throws IOException {
        bos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(bos);
        raceLog = new RaceLogImpl("Test Race Log");
        eventFactory = RaceLogEventFactory.INSTANCE;
    }
    
    @Test
    public void testSimpleRaceLogSerialization() throws IOException, ClassNotFoundException {
        oos.writeObject(raceLog);
        ObjectInputStream ois = getObjectInputStream();
        RaceLog rl = (RaceLog) ois.readObject();
        raceLog.lockForRead();
        rl.lockForRead();
        try {
            assertEquals(Util.size(raceLog.getRawFixes()), Util.size(rl.getRawFixes()));
        } finally {
            rl.unlockAfterRead();
            raceLog.unlockAfterRead();
        }
    }

    @Test
    public void testRaceLogSerializationWithSingleEvent() throws IOException, ClassNotFoundException {
        RaceLogStartTimeEvent startTimeEvent = eventFactory.createStartTimeEvent(MillisecondsTimePoint.now(),
                new RaceLogEventAuthorImpl("Author Name", /* priority */0), /* passId */1, /* startTime */
                MillisecondsTimePoint.now());
        raceLog.add(startTimeEvent);
        oos.writeObject(raceLog);
        ObjectInputStream ois = getObjectInputStream();
        RaceLog rl = (RaceLog) ois.readObject();
        raceLog.lockForRead();
        rl.lockForRead();
        try {
            assertEquals(Util.size(raceLog.getRawFixes()), Util.size(rl.getRawFixes()));
            assertEquals(((RaceLogStartTimeEvent) raceLog.getFirstRawFix()).getStartTime(), ((RaceLogStartTimeEvent) rl.getFirstRawFix()).getStartTime());
        } finally {
            rl.unlockAfterRead();
            raceLog.unlockAfterRead();
        }
    }

    @Test
    public void testRaceLogSerializationWithEventContainingDeviceIdentifier() throws IOException, ClassNotFoundException {
    	DeviceCompetitorMappingEvent mappingEvent = eventFactory.createDeviceCompetitorMappingEvent(
        		MillisecondsTimePoint.now(), new RaceLogEventAuthorImpl("Author Name", /* priority */0), new SmartphoneImeiIdentifier("1948364938463903"),
        		AbstractLeaderboardTest.createCompetitor("Test Competitor"), 0, new MillisecondsTimePoint(0), new MillisecondsTimePoint(10));
        raceLog.add(mappingEvent);
        oos.writeObject(raceLog);
        ObjectInputStream ois = getObjectInputStream();
        RaceLog rl = (RaceLog) ois.readObject();
        raceLog.lockForRead();
        rl.lockForRead();
        try {
        	DeviceCompetitorMappingEvent expected = ((DeviceCompetitorMappingEvent) raceLog.getFirstRawFix());
        	DeviceCompetitorMappingEvent actual = ((DeviceCompetitorMappingEvent) rl.getFirstRawFix());
        	
            assertEquals(Util.size(raceLog.getRawFixes()), Util.size(rl.getRawFixes()));
            assertEquals(expected.getDevice().getIdentifierType(), actual.getDevice().getIdentifierType());
        } finally {
            rl.unlockAfterRead();
            raceLog.unlockAfterRead();
        }
    }

    private ObjectInputStream getObjectInputStream() throws IOException {
        return new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
    }
}
